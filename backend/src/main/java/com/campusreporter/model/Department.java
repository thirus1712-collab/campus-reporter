package com.campusreporter.model;

public enum Department {
    ELECTRICAL("Electrical", "⚡"),
    PLUMBING("Plumbing", "🚰"),
    CLEANING("Cleaning", "🧹"),
    IT_SUPPORT("IT & Technical Support", "💻");

    private final String displayName;
    private final String icon;

    Department(String displayName, String icon) {
        this.displayName = displayName;
        this.icon = icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getIcon() {
        return icon;
    }

    public static Department fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        String clean = text.trim().toUpperCase().replace(" ", "_").replace("&", "").replace("__", "_");
        for (Department d : Department.values()) {
            if (d.name().equalsIgnoreCase(clean) || d.displayName.equalsIgnoreCase(text.trim())) {
                return d;
            }
        }
        if (clean.contains("ELECTRICAL")) return ELECTRICAL;
        if (clean.contains("PLUMB")) return PLUMBING;
        if (clean.contains("CLEAN")) return CLEANING;
        if (clean.contains("IT") || clean.contains("TECH")) return IT_SUPPORT;
        return null;
    }
}
