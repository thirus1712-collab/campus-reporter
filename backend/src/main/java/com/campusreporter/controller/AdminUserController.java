package com.campusreporter.controller;

import com.campusreporter.dto.CreateUserRequestDTO;
import com.campusreporter.dto.UserResponseDTO;
import com.campusreporter.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final UserService userService;

    @Autowired
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * GET /api/admin/users
     * Returns all campus user accounts for authorized administrators.
     */
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> getAllUsers(
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        List<UserResponseDTO> users = userService.getAllUsers(authHeader);
        return ResponseEntity.ok(users);
    }

    /**
     * POST /api/admin/users
     * Creates a new user account with secure server-side password hashing.
     */
    @PostMapping
    public ResponseEntity<UserResponseDTO> createUser(
        @Valid @RequestBody CreateUserRequestDTO request,
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        UserResponseDTO created = userService.createUser(request, authHeader);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PATCH or POST /api/admin/users/{id}/deactivate
     * Deactivates an existing user account and revokes active sessions.
     */
    @RequestMapping(value = "/{id}/deactivate", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<UserResponseDTO> deactivateUser(
        @PathVariable("id") Long id,
        @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        UserResponseDTO deactivated = userService.deactivateUser(id, authHeader);
        return ResponseEntity.ok(deactivated);
    }
}
