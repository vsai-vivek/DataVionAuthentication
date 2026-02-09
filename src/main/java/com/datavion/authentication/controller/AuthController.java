package com.datavion.authentication.controller;

import com.datavion.authentication.dto.*;
import com.datavion.authentication.security.UserPrincipal;
import com.datavion.authentication.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization endpoints")
public class AuthController {
    
    private final AuthService authService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refreshToken(@RequestBody RefreshTokenRequest request) {
        AuthResponse response = authService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/logout")
    @Operation(summary = "Logout user")
    public ResponseEntity<Void> logout(@RequestBody(required = false) RefreshTokenRequest request) {
        String refreshToken = request != null ? request.getRefreshToken() : null;
        authService.logout(refreshToken);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user information")
    public ResponseEntity<UserResponse> getCurrentUser(@AuthenticationPrincipal UserPrincipal userPrincipal) {
        // Map user to response (you might want to create a separate service method for this)
        UserResponse userResponse = UserResponse.builder()
                .id(userPrincipal.getUser().getId())
                .username(userPrincipal.getUser().getUsername())
                .email(userPrincipal.getUser().getEmail())
                .emailVerified(userPrincipal.getUser().getEmailVerified())
                .accountLocked(userPrincipal.getUser().getAccountLocked())
                .lastLoginAt(userPrincipal.getUser().getLastLoginAt())
                .source(userPrincipal.getUser().getSource().name())
                .createdAt(userPrincipal.getUser().getCreatedAt())
                .build();
        
        return ResponseEntity.ok(userResponse);
    }
}