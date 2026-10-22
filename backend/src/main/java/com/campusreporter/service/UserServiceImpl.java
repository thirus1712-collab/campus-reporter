package com.campusreporter.service;

import com.campusreporter.dto.CreateUserRequestDTO;
import com.campusreporter.dto.UserResponseDTO;
import com.campusreporter.entity.User;
import com.campusreporter.exception.ResourceNotFoundException;
import com.campusreporter.exception.UnauthorizedException;
import com.campusreporter.exception.ValidationException;
import com.campusreporter.model.Department;
import com.campusreporter.model.UserRole;
import com.campusreporter.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;
    private final AuthService authService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(
        UserRepository userRepository,
        AuthService authService,
        PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.authService = authService;
        this.passwordEncoder = passwordEncoder;
    }

    private User requireAdmin(String authHeader) {
        User caller = authService.validateToken(authHeader);
        if (caller == null || caller.getRole() != UserRole.ADMIN || !caller.isActive()) {
            throw new UnauthorizedException("Access denied: Campus Administrator authorization required.");
        }
        return caller;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAllUsers(String authHeader) {
        requireAdmin(authHeader);
        return userRepository.findAllByOrderByCreatedAtDesc().stream()
            .map(UserResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public UserResponseDTO createUser(CreateUserRequestDTO request, String authHeader) {
        requireAdmin(authHeader);

        if (request == null) {
            throw new ValidationException("Account creation payload is required.");
        }

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ValidationException("Email / account identifier is required.");
        }

        String email = request.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmailIgnoreCase(email)) {
            throw new ValidationException("An account with email '" + email + "' already exists.");
        }

        if (request.getFullName() == null || request.getFullName().trim().isEmpty()) {
            throw new ValidationException("Full name / display name is required.");
        }

        UserRole role = UserRole.fromString(request.getRole());
        if (role == null) {
            throw new ValidationException("Invalid account role: " + request.getRole());
        }

        Department department = null;
        if (role == UserRole.STUDENT || role == UserRole.ADMIN) {
            department = null;
        } else {
            if (request.getDepartment() != null && !request.getDepartment().trim().isEmpty()) {
                department = Department.fromString(request.getDepartment());
            }
            if (department == null) {
                department = role.toDepartment();
            }
        }

        if (request.getPassword() == null || request.getPassword().trim().length() < 6) {
            throw new ValidationException("Initial password must be at least 6 characters long.");
        }

        String phone = (request.getPhone() != null && !request.getPhone().trim().isEmpty())
            ? request.getPhone().trim()
            : null;

        String encodedPassword = passwordEncoder.encode(request.getPassword().trim());

        User newUser = new User(
            email,
            encodedPassword,
            request.getFullName().trim(),
            role,
            department,
            phone
        );
        newUser.setActive(true);
        newUser.setCreatedAt(LocalDateTime.now());

        User saved = userRepository.save(newUser);
        logger.info("Admin created new account id={}, email={}, role={}", saved.getId(), saved.getEmail(), saved.getRole());

        return UserResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public UserResponseDTO deactivateUser(Long userId, String authHeader) {
        User caller = requireAdmin(authHeader);

        if (userId == null) {
            throw new ValidationException("User ID is required for deactivation.");
        }

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("User account not found with ID: " + userId));

        // Prevent self-deactivation
        if (caller.getId().equals(user.getId()) || caller.getEmail().equalsIgnoreCase(user.getEmail())) {
            throw new ValidationException("Administrators cannot deactivate their own account while signed in.");
        }

        // Prevent deactivating the last active administrator
        if (user.getRole() == UserRole.ADMIN) {
            long activeAdminCount = userRepository.countByRoleAndActiveTrue(UserRole.ADMIN);
            if (activeAdminCount <= 1) {
                throw new ValidationException("Cannot deactivate the last active administrator account. System requires at least one active administrator.");
            }
        }

        if (!user.isActive()) {
            return UserResponseDTO.fromEntity(user);
        }

        user.setActive(false);
        User updated = userRepository.save(user);

        // Terminate any active sessions for this deactivated user
        authService.invalidateUserSessions(user.getId());

        logger.info("Admin deactivated account id={}, email={}, role={}", updated.getId(), updated.getEmail(), updated.getRole());
        return UserResponseDTO.fromEntity(updated);
    }
}
