package com.datavion.authentication.service;

import com.datavion.authentication.dto.UserResponse;
import com.datavion.authentication.entity.Role;
import com.datavion.authentication.entity.User;
import com.datavion.authentication.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findByDeletedAtIsNull(pageable)
                .map(this::mapToUserResponse);
    }
    
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (user.isDeleted()) {
            throw new RuntimeException("User not found");
        }
        
        return mapToUserResponse(user);
    }
    
    @Transactional
    public void unlockUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setAccountLocked(false);
        user.setLockedAt(null);
        user.setFailedLoginAttempts(0);
        
        userRepository.save(user);
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