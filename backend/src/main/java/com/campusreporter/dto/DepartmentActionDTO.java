package com.campusreporter.dto;

import java.util.ArrayList;
import java.util.List;

public class DepartmentActionDTO {
    private String staffName;
    private String staffEmail;
    private String notes;
    private String reason;

    private String evidenceUrl;
    private List<String> evidenceImages = new ArrayList<>();
    private String evidenceNotes;
    private String acknowledgement;
    private String feedback;

    public DepartmentActionDTO() {}

    public String getStaffName() {
        return staffName;
    }

    public void setStaffName(String staffName) {
        this.staffName = staffName;
    }

    public String getStaffEmail() {
        return staffEmail;
    }

    public void setStaffEmail(String staffEmail) {
        this.staffEmail = staffEmail;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getEvidenceUrl() {
        return evidenceUrl;
    }

    public void setEvidenceUrl(String evidenceUrl) {
        this.evidenceUrl = evidenceUrl;
    }

    public String getEvidenceNotes() {
        return evidenceNotes;
    }

    public void setEvidenceNotes(String evidenceNotes) {
        this.evidenceNotes = evidenceNotes;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setRepairEvidenceUrl(String url) {
        this.evidenceUrl = url;
    }

    public String getRepairEvidenceUrl() {
        return evidenceUrl;
    }

    public void setRepairNotes(String notes) {
        this.evidenceNotes = notes;
    }

    public String getRepairNotes() {
        return evidenceNotes;
    }

    public List<String> getEvidenceImages() {
        return evidenceImages;
    }

    public void setEvidenceImages(List<String> evidenceImages) {
        this.evidenceImages = evidenceImages != null ? evidenceImages : new ArrayList<>();
    }

    public String getAcknowledgement() {
        return acknowledgement != null ? acknowledgement : (evidenceNotes != null ? evidenceNotes : notes);
    }

    public void setAcknowledgement(String acknowledgement) {
        this.acknowledgement = acknowledgement;
    }
}
