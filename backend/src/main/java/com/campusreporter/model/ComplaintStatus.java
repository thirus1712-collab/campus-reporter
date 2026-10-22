package com.campusreporter.model;

public enum ComplaintStatus {
    SUBMITTED("Submitted"),
    UNDER_REVIEW("Under Review"),
    IN_PROGRESS("In Progress"),
    AWAITING_VERIFICATION("Awaiting Student Verification"),
    RESOLVED("Resolved"),
    CLOSED("Closed"),
    DECLINED("Declined");

    private final String displayName;

    ComplaintStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static ComplaintStatus fromString(String text) {
        if (text == null || text.trim().isEmpty()) {
            return SUBMITTED;
        }
        String clean = text.trim().toUpperCase().replace(" ", "_");
        for (ComplaintStatus s : ComplaintStatus.values()) {
            if (s.name().equalsIgnoreCase(clean) || s.displayName.equalsIgnoreCase(text.trim())) {
                return s;
            }
        }
        return SUBMITTED;
    }
}
