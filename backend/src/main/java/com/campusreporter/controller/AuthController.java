package com.campusreporter.controller;

import com.campusreporter.dto.AuthResponseDTO;
import com.campusreporter.dto.LoginRequestDTO;
import com.campusreporter.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    @Autowired
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * POST /api/auth/login
     * Authenticates user against BCrypt hash in PostgreSQL and returns authorized role and token.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        AuthResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/auth/me
     * Returns current user details if token is valid.
     */
    @GetMapping("/me")
    public ResponseEntity<AuthResponseDTO> getMe(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        AuthResponseDTO response = authService.getMe(authHeader);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /api/auth/logout
     * Invalidates the user session token.
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponseDTO> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        authService.logout(authHeader);
        return ResponseEntity.ok(AuthResponseDTO.success(null, null, null, null, null, null, null));
    }
}
