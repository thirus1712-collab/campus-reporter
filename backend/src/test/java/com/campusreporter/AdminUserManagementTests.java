package com.campusreporter;

import com.campusreporter.dto.CreateUserRequestDTO;
import com.campusreporter.dto.UserResponseDTO;
import com.campusreporter.entity.User;
import com.campusreporter.exception.UnauthorizedException;
import com.campusreporter.exception.ValidationException;
import com.campusreporter.model.Department;
import com.campusreporter.model.UserRole;
import com.campusreporter.repository.UserRepository;
import com.campusreporter.service.AuthService;
import com.campusreporter.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AdminUserManagementTests {

    private UserRepository userRepository;
    private AuthService authService;
    private PasswordEncoder passwordEncoder;
    private UserServiceImpl userService;

    private User adminUser;
    private User studentUser;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        authService = mock(AuthService.class);
        passwordEncoder = mock(PasswordEncoder.class);

        userService = new UserServiceImpl(userRepository, authService, passwordEncoder);

        adminUser = new User("admin@smartcampus.edu", "hashed_pwd", "Campus Admin", UserRole.ADMIN, null, "1234567890");
        adminUser.setId(1L);
        adminUser.setActive(true);

        studentUser = new User("student@smartcampus.edu", "hashed_pwd", "Student One", UserRole.STUDENT, null, "1234567891");
        studentUser.setId(2L);
        studentUser.setActive(true);
    }

    @Test
    @DisplayName("Admin can view all users, and passwords/hashes are never exposed")
    void testGetAllUsers_Success() {
        when(authService.validateToken("admin_token")).thenReturn(adminUser);
        when(userRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(adminUser, studentUser));

        List<UserResponseDTO> users = userService.getAllUsers("admin_token");
        assertEquals(2, users.size());
        assertEquals("admin@smartcampus.edu", users.get(0).getEmail());
        assertEquals("student@smartcampus.edu", users.get(1).getEmail());

        // Verify password is not in DTO
        assertNotNull(users.get(0).getEmail());
    }

    @Test
    @DisplayName("Non-admin users cannot access user management list")
    void testGetAllUsers_ForbiddenForNonAdmin() {
        when(authService.validateToken("student_token")).thenReturn(studentUser);

        assertThrows(UnauthorizedException.class, () -> userService.getAllUsers("student_token"));
    }

    @Test
    @DisplayName("Admin can create new accounts with encrypted password")
    void testCreateUser_Success() {
        when(authService.validateToken("admin_token")).thenReturn(adminUser);
        when(userRepository.existsByEmailIgnoreCase("newtech@smartcampus.edu")).thenReturn(false);
        when(passwordEncoder.encode("TechSecret@123")).thenReturn("securely_hashed_bcrypt_string");

        User savedUser = new User("newtech@smartcampus.edu", "securely_hashed_bcrypt_string", "Tech Specialist", UserRole.IT_SUPPORT, Department.IT_SUPPORT, "9988776655");
        savedUser.setId(10L);
        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        CreateUserRequestDTO request = new CreateUserRequestDTO(
            "newtech@smartcampus.edu",
            "Tech Specialist",
            "IT_SUPPORT",
            "IT_SUPPORT",
            "9988776655",
            "TechSecret@123"
        );

        UserResponseDTO response = userService.createUser(request, "admin_token");
        assertNotNull(response);
        assertEquals("newtech@smartcampus.edu", response.getEmail());
        assertEquals("IT_SUPPORT", response.getRole());
        assertTrue(response.isActive());

        verify(passwordEncoder).encode("TechSecret@123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    @DisplayName("Create user rejects duplicate email addresses")
    void testCreateUser_DuplicateEmailRejected() {
        when(authService.validateToken("admin_token")).thenReturn(adminUser);
        when(userRepository.existsByEmailIgnoreCase("existing@smartcampus.edu")).thenReturn(true);

        CreateUserRequestDTO request = new CreateUserRequestDTO(
            "existing@smartcampus.edu",
            "Existing User",
            "STUDENT",
            null,
            null,
            "Password@123"
        );

        assertThrows(ValidationException.class, () -> userService.createUser(request, "admin_token"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Admin can deactivate an active account and revoke active sessions")
    void testDeactivateUser_Success() {
        when(authService.validateToken("admin_token")).thenReturn(adminUser);

        User techUser = new User("tech@smartcampus.edu", "hash", "Tech Guy", UserRole.ELECTRICAL, Department.ELECTRICAL, "1122334455");
        techUser.setId(5L);
        techUser.setActive(true);

        when(userRepository.findById(5L)).thenReturn(Optional.of(techUser));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        UserResponseDTO deactivated = userService.deactivateUser(5L, "admin_token");
        assertNotNull(deactivated);
        assertFalse(deactivated.isActive());

        verify(authService).invalidateUserSessions(5L);
        verify(userRepository).save(techUser);
    }

    @Test
    @DisplayName("Administrator cannot deactivate their own logged in account")
    void testDeactivateUser_CannotDeactivateSelf() {
        when(authService.validateToken("admin_token")).thenReturn(adminUser);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));

        ValidationException ex = assertThrows(ValidationException.class, () -> userService.deactivateUser(1L, "admin_token"));
        assertTrue(ex.getMessage().contains("cannot deactivate their own account"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Administrator cannot deactivate the last active administrator")
    void testDeactivateUser_CannotDeactivateLastAdmin() {
        User anotherAdminCaller = new User("admin2@smartcampus.edu", "hash", "Admin Two", UserRole.ADMIN, null, null);
        anotherAdminCaller.setId(99L);
        anotherAdminCaller.setActive(true);

        when(authService.validateToken("admin2_token")).thenReturn(anotherAdminCaller);
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(userRepository.countByRoleAndActiveTrue(UserRole.ADMIN)).thenReturn(1L);

        ValidationException ex = assertThrows(ValidationException.class, () -> userService.deactivateUser(1L, "admin2_token"));
        assertTrue(ex.getMessage().contains("Cannot deactivate the last active administrator"));
        verify(userRepository, never()).save(any(User.class));
    }
}
