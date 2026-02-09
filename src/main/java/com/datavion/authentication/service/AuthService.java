package com.datavion.authentication.service;

import com.datavion.authentication.dto.*;
import com.datavion.authentication.entity.RefreshToken;
import com.datavion.authentication.entity.Role;
import com.datavion.authentication.entity.User;
import com.datavion.authentication.repository.RefreshTokenRepository;
import com.datavion.authentication.repository.RoleRepository;
import com.datavion.authentication.repository.UserRepository;
import com.datavion.authentication.security.UserPrincipal;
import com.datavion.authentication.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    
    @Value("${app.jwt.access-token-expiration}")
    private Long accessTokenExpiration;
    
    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;
    
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if user already exists
        if (userRepository.existsByUsernameAndDeletedAtIsNull(request.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        
        if (userRepository.existsByEmailAndDeletedAtIsNull(request.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        // Get default USER role
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Default USER role not found"));
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false) // In production, this should be false and require email verification
                .roles(Set.of(userRole))
                .build();
        
        user = userRepository.save(user);
        
        // Generate tokens
        UserPrincipal userPrincipal = new UserPrincipal(user);
        String accessToken = jwtUtil.generateAccessToken(userPrincipal);
        String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);
        
        // Save refresh token
        saveRefreshToken(user, refreshToken);
        
        log.info("User registered successfully: {}", user.getUsername());
        
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(accessTokenExpiration / 1000)
                .user(mapToUserResponse(user))
                .build();
    }
    
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsernameOrEmail(),
                            request.getPassword()
                    )
            );
            
            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();
            
            // Update last login info
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.save(user);
            
            // Generate tokens
            String accessToken = jwtUtil.generateAccessToken(userPrincipal);
            String refreshToken = jwtUtil.generateRefreshToken(userPrincipal);
            
            // Revoke old refresh tokens and save new one
            refreshTokenRepository.revokeAllByUser(user);
            saveRefreshToken(user, refreshToken);
            
            log.info("User logged in successfully: {}", user.getUsername());
            
            return AuthResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .expiresIn(accessTokenExpiration / 1000)
                    .user(mapToUserResponse(user))
                    .build();
                    
        } catch (BadCredentialsException e) {
            // Handle failed login attempt
            handleFailedLogin(request.getUsernameOrEmail());
            throw new BadCredentialsException("Invalid credentials");
        }
    }
    
    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        String tokenHash = passwordEncoder.encode(refreshToken);
        
        RefreshToken storedToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        
        if (!storedToken.isValid()) {
            throw new RuntimeException("Refresh token is expired or revoked");
        }
        
        User user = storedToken.getUser();
        UserPrincipal userPrincipal = new UserPrincipal(user);
        
        // Generate new tokens
        String newAccessToken = jwtUtil.generateAccessToken(userPrincipal);
        String newRefreshToken = jwtUtil.generateRefreshToken(userPrincipal);
        
        // Revoke old refresh token and save new one
        storedToken.setRevoked(true);
        refreshTokenRepository.save(storedToken);
        saveRefreshToken(user, newRefreshToken);
        
        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .expiresIn(accessTokenExpiration / 1000)
                .user(mapToUserResponse(user))
                .build();
    }
    
    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken != null) {
            String tokenHash = passwordEncoder.encode(refreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash)
                    .ifPresent(token -> {
                        token.setRevoked(true);
                        refreshTokenRepository.save(token);
                    });
        }
    }
    
    private void saveRefreshToken(User user, String refreshToken) {
        String tokenHash = passwordEncoder.encode(refreshToken);
        
        RefreshToken token = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(LocalDateTime.now().plusSeconds(refreshTokenExpiration / 1000))
                .build();
        
        refreshTokenRepository.save(token);
    }
    
    private void handleFailedLogin(String usernameOrEmail) {
        userRepository.findByUsernameOrEmailAndDeletedAtIsNull(usernameOrEmail)
                .ifPresent(user -> {
                    user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                    
                    if (user.getFailedLoginAttempts() >= 5) {
                        user.setAccountLocked(true);
                        user.setLockedAt(LocalDateTime.now());
                        log.warn("Account locked due to too many failed attempts: {}", user.getUsername());
                    }
                    
                    userRepository.save(user);
                });
    }
    
    private UserResponse mapToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .emailVerified(user.getEmailVerified())
                .accountLocked(user.getAccountLocked())
                .lastLoginAt(user.getLastLoginAt())
                .source(user.getSource().name())
                .roles(user.getRoles().stream()
                        .map(Role::getName)
                        .collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}