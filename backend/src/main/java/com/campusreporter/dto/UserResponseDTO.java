package com.campusreporter.dto;

import com.campusreporter.entity.User;
import java.time.LocalDateTime;

public class UserResponseDTO {

    private Long id;
    private String email;
    private String fullName;
    private String role;
    private String roleDisplayName;
    private String department;
    private String departmentDisplayName;
    private String phone;
    private boolean active;
    private LocalDateTime createdAt;
    private LocalDateTime lastLoginAt;

    public UserResponseDTO() {}

    public UserResponseDTO(
        Long id,
        String email,
        String fullName,
        String role,
        String roleDisplayName,
        String department,
        String departmentDisplayName,
        String phone,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime lastLoginAt
    ) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.role = role;
        this.roleDisplayName = roleDisplayName;
        this.department = department;
        this.departmentDisplayName = departmentDisplayName;
        this.phone = phone;
        this.active = active;
        this.createdAt = createdAt;
        this.lastLoginAt = lastLoginAt;
    }

    public static UserResponseDTO fromEntity(User user) {
        if (user == null) return null;

        String roleStr = user.getRole() != null ? user.getRole().name() : null;
        String roleDisplay = user.getRole() != null ? user.getRole().getDisplayName() : null;
        String deptStr = user.getDepartment() != null ? user.getDepartment().name() : null;
        String deptDisplay = user.getDepartment() != null ? user.getDepartment().getDisplayName() : null;

        return new UserResponseDTO(
            user.getId(),
            user.getEmail(),
            user.getFullName(),
            roleStr,
            roleDisplay,
            deptStr,
            deptDisplay,
            user.getPhone(),
            user.isActive(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getRoleDisplayName() {
        return roleDisplayName;
    }

    public void setRoleDisplayName(String roleDisplayName) {
        this.roleDisplayName = roleDisplayName;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartmentDisplayName() {
        return departmentDisplayName;
    }

    public void setDepartmentDisplayName(String departmentDisplayName) {
        this.departmentDisplayName = departmentDisplayName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLoginAt() {
        return lastLoginAt;
    }

    public void setLastLoginAt(LocalDateTime lastLoginAt) {
        this.lastLoginAt = lastLoginAt;
    }
}
