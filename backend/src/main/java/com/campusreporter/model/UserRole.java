package com.campusreporter.model;

public enum UserRole {
    STUDENT("Student", "student/index.html"),
    ELECTRICAL("Electrical Department", "department/index.html?dept=ELECTRICAL"),
    PLUMBING("Plumbing Department", "department/index.html?dept=PLUMBING"),
    CLEANING("Cleaning Department", "department/index.html?dept=CLEANING"),
    IT_SUPPORT("IT & Technical Support", "department/index.html?dept=IT_SUPPORT"),
    ADMIN("Campus Administrator", "admin/index.html");

    private final String displayName;
    private final String defaultPortalUrl;

    UserRole(String displayName, String defaultPortalUrl) {
        this.displayName = displayName;
        this.defaultPortalUrl = defaultPortalUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDefaultPortalUrl() {
        return defaultPortalUrl;
    }

    public Department toDepartment() {
        return switch (this) {
            case ELECTRICAL -> Department.ELECTRICAL;
            case PLUMBING -> Department.PLUMBING;
            case CLEANING -> Department.CLEANING;
            case IT_SUPPORT -> Department.IT_SUPPORT;
            default -> null;
        };
    }

    public static UserRole fromString(String roleStr) {
        if (roleStr == null || roleStr.trim().isEmpty()) {
            return null;
        }
        String clean = roleStr.trim().toUpperCase().replace(" ", "_");
        for (UserRole r : UserRole.values()) {
            if (r.name().equalsIgnoreCase(clean)) {
                return r;
            }
        }
        return null;
    }
}
