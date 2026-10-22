package com.campusreporter.dto;

public class AuthResponseDTO {

    private boolean success;
    private String token;
    private String email;
    private String fullName;
    private String role;
    private String roleLabel;
    private String department;
    private String portalUrl;
    private String message;

    public AuthResponseDTO() {}

    public static AuthResponseDTO success(String token, String email, String fullName, String role, String roleLabel, String department, String portalUrl) {
        AuthResponseDTO res = new AuthResponseDTO();
        res.success = true;
        res.token = token;
        res.email = email;
        res.fullName = fullName;
        res.role = role;
        res.roleLabel = roleLabel;
        res.department = department;
        res.portalUrl = portalUrl;
        res.message = "Authentication successful";
        return res;
    }

    public static AuthResponseDTO failure(String message) {
        AuthResponseDTO res = new AuthResponseDTO();
        res.success = false;
        res.message = message;
        return res;
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
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

    public String getRoleLabel() {
        return roleLabel;
    }

    public void setRoleLabel(String roleLabel) {
        this.roleLabel = roleLabel;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getPortalUrl() {
        return portalUrl;
    }

    public void setPortalUrl(String portalUrl) {
        this.portalUrl = portalUrl;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
