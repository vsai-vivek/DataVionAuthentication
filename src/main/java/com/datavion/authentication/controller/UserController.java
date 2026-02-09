package com.datavion.authentication.controller;

import com.datavion.authentication.dto.UserResponse;
import com.datavion.authentication.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User Management", description = "User management endpoints")
public class UserController {
    
    private final UserService userService;
    
    @GetMapping
    @Operation(summary = "Get all users")
    @PreAuthorize("hasAuthority('users:READ')")
    public ResponseEntity<Page<UserResponse>> getAllUsers(Pageable pageable) {
        Page<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity.ok(users);
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID")
    @PreAuthorize("hasAuthority('users:READ')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }
    
    @PostMapping("/{id}/unlock")
    @Operation(summary = "Unlock user account")
    @PreAuthorize("hasAuthority('users:UPDATE')")
    public ResponseEntity<Void> unlockUser(@PathVariable Long id) {
        userService.unlockUser(id);
        return ResponseEntity.ok().build();
    }
}