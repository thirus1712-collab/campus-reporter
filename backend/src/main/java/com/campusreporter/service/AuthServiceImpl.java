package com.campusreporter.service;

import com.campusreporter.dto.AuthResponseDTO;
import com.campusreporter.dto.LoginRequestDTO;
import com.campusreporter.entity.User;
import com.campusreporter.exception.AuthenticationException;
import com.campusreporter.exception.UnauthorizedException;
import com.campusreporter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom secureRandom = new SecureRandom();

    // Active session store: token -> SessionData
    private final Map<String, SessionData> activeSessions = new ConcurrentHashMap<>();

    private static class SessionData {
        final Long userId;
        final String email;
        final LocalDateTime expiry;

        SessionData(Long userId, String email, LocalDateTime expiry) {
            this.userId = userId;
            this.email = email;
            this.expiry = expiry;
        }

        boolean isExpired() {
            return LocalDateTime.now().isAfter(expiry);
        }
    }

    @Autowired
    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public AuthResponseDTO login(LoginRequestDTO request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        String email = request.getEmail().trim().toLowerCase();
        Optional<User> userOpt = userRepository.findByEmailIgnoreCase(email);

        // Security check: Generic error message to prevent account enumeration
        if (userOpt.isEmpty() || !userOpt.get().isActive()) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        User user = userOpt.get();
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password.");
        }

        // Generate cryptographically secure token
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        String token = "scr_" + Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);

        // Session valid for 24 hours
        activeSessions.put(token, new SessionData(user.getId(), user.getEmail(), LocalDateTime.now().plusHours(24)));

        user.setLastLoginAt(LocalDateTime.now());
        userRepository.save(user);

        String departmentName = user.getDepartment() != null ? user.getDepartment().name() : null;

        return AuthResponseDTO.success(
            token,
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getRole().getDisplayName(),
            departmentName,
            user.getRole().getDefaultPortalUrl()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponseDTO getMe(String token) {
        User user = validateToken(token);
        String departmentName = user.getDepartment() != null ? user.getDepartment().name() : null;

        return AuthResponseDTO.success(
            token,
            user.getEmail(),
            user.getFullName(),
            user.getRole().name(),
            user.getRole().getDisplayName(),
            departmentName,
            user.getRole().getDefaultPortalUrl()
        );
    }

    @Override
    public void logout(String token) {
        if (token != null) {
            String cleanToken = cleanBearerToken(token);
            activeSessions.remove(cleanToken);
        }
    }

    @Override
    public User validateToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            throw new AuthenticationException("Authentication required. Please sign in.");
        }

        String cleanToken = cleanBearerToken(token);
        SessionData session = activeSessions.get(cleanToken);

        if (session == null || session.isExpired()) {
            activeSessions.remove(cleanToken);
            throw new AuthenticationException("Session has expired. Please sign in again.");
        }

        return userRepository.findById(session.userId)
            .filter(User::isActive)
            .orElseThrow(() -> new AuthenticationException("User account not found or deactivated."));
    }

    @Override
    public void invalidateUserSessions(Long userId) {
        if (userId != null) {
            activeSessions.entrySet().removeIf(entry -> userId.equals(entry.getValue().userId));
        }
    }

    private String cleanBearerToken(String token) {
        String clean = token.trim();
        if (clean.toLowerCase().startsWith("bearer ")) {
            return clean.substring(7).trim();
        }
        return clean;
    }
}
