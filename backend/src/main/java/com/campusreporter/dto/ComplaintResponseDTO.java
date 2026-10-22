package com.campusreporter.dto;

import com.campusreporter.entity.Complaint;
import java.time.LocalDateTime;
import java.util.List;

public class ComplaintResponseDTO {

    private boolean success = true;
    private Long id;
    private String ticketId;
    private String department;
    private String departmentLabel;
    private String campusBlock;
    private String floorLevel;
    private String specificLocation;
    private String issueTitle;
    private String description;
    private String studentPhone;
    private String studentEmail;
    private String studentName;
    private boolean emergency;
    private String status;
    private String statusCode;
    private List<String> imageUrls;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private String acceptedBy;
    private LocalDateTime acceptedAt;
    private String inProgressBy;
    private LocalDateTime inProgressAt;
    private LocalDateTime verificationRequestedAt;
    private String repairEvidenceUrl;
    private List<String> repairEvidenceImages;
    private String repairNotes;
    private String resolvedBy;
    private LocalDateTime resolvedAt;
    private String studentFeedback;
    private String declinedReason;
    private String declinedBy;
    private LocalDateTime declinedAt;
    private LocalDateTime workDeadline;
    private boolean escalatedToAdmin;
    private LocalDateTime escalatedAt;
    private String escalationReason;
    private LocalDateTime reworkStartedAt;
    private LocalDateTime serverTime;
    private boolean overdue;
    private Long remainingSeconds;

    public ComplaintResponseDTO() {}

    public static ComplaintResponseDTO fromEntity(Complaint c) {
        if (c == null) return null;
        ComplaintResponseDTO dto = new ComplaintResponseDTO();
        dto.setSuccess(true);
        dto.setId(c.getId());
        dto.setTicketId(c.getTicketId());
        dto.setDepartment(c.getDepartment() != null ? c.getDepartment().name() : "");
        dto.setDepartmentLabel(c.getDepartment() != null ? c.getDepartment().getDisplayName() : "");
        dto.setCampusBlock(c.getCampusBlock());
        dto.setFloorLevel(c.getFloorLevel());
        dto.setSpecificLocation(c.getSpecificLocation());
        dto.setIssueTitle(c.getIssueTitle());
        dto.setDescription(c.getDescription());
        dto.setStudentPhone(c.getStudentPhone());
        dto.setStudentEmail(c.getStudentEmail());
        dto.setStudentName(c.getStudentName());
        dto.setEmergency(c.isEmergency());
        dto.setStatus(c.getStatus() != null ? c.getStatus().getDisplayName() : "Submitted");
        dto.setStatusCode(c.getStatus() != null ? c.getStatus().name() : "SUBMITTED");
        dto.setImageUrls(c.getImageUrls());
        dto.setSubmittedAt(c.getSubmittedAt());
        dto.setUpdatedAt(c.getUpdatedAt());
        dto.setAcceptedBy(c.getAcceptedBy());
        dto.setAcceptedAt(c.getAcceptedAt());
        dto.setInProgressBy(c.getInProgressBy());
        dto.setInProgressAt(c.getInProgressAt());
        dto.setVerificationRequestedAt(c.getVerificationRequestedAt());
        dto.setRepairEvidenceUrl(c.getRepairEvidenceUrl());
        dto.setRepairEvidenceImages(c.getRepairEvidenceImages());
        dto.setRepairNotes(c.getRepairNotes());
        dto.setResolvedBy(c.getResolvedBy());
        dto.setResolvedAt(c.getResolvedAt());
        dto.setStudentFeedback(c.getStudentFeedback());
        dto.setDeclinedReason(c.getDeclinedReason());
        dto.setDeclinedBy(c.getDeclinedBy());
        dto.setDeclinedAt(c.getDeclinedAt());
        dto.setWorkDeadline(c.getWorkDeadline());
        dto.setEscalatedToAdmin(c.isEscalatedToAdmin());
        dto.setEscalatedAt(c.getEscalatedAt());
        dto.setEscalationReason(c.getEscalationReason());
        dto.setReworkStartedAt(c.getReworkStartedAt());
        LocalDateTime now = LocalDateTime.now();
        dto.setServerTime(now);
        boolean isOverdue = c.getWorkDeadline() != null
            && now.isAfter(c.getWorkDeadline())
            && c.getVerificationRequestedAt() == null
            && c.getStatus() != com.campusreporter.model.ComplaintStatus.CLOSED
            && c.getStatus() != com.campusreporter.model.ComplaintStatus.RESOLVED
            && c.getStatus() != com.campusreporter.model.ComplaintStatus.DECLINED;
        dto.setOverdue(isOverdue);
        if (c.getWorkDeadline() != null) {
            long remaining = java.time.Duration.between(now, c.getWorkDeadline()).getSeconds();
            dto.setRemainingSeconds(remaining);
        }
        return dto;
    }

    // Getters and Setters

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTicketId() {
        return ticketId;
    }

    public void setTicketId(String ticketId) {
        this.ticketId = ticketId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getDepartmentLabel() {
        return departmentLabel;
    }

    public void setDepartmentLabel(String departmentLabel) {
        this.departmentLabel = departmentLabel;
    }

    public String getCampusBlock() {
        return campusBlock;
    }

    public String getBlock() {
        return campusBlock;
    }

    public void setCampusBlock(String campusBlock) {
        this.campusBlock = campusBlock;
    }

    public String getFloorLevel() {
        return floorLevel;
    }

    public String getFloor() {
        return floorLevel;
    }

    public void setFloorLevel(String floorLevel) {
        this.floorLevel = floorLevel;
    }

    public String getSpecificLocation() {
        return specificLocation;
    }

    public void setSpecificLocation(String specificLocation) {
        this.specificLocation = specificLocation;
    }

    public String getIssueTitle() {
        return issueTitle;
    }

    public void setIssueTitle(String issueTitle) {
        this.issueTitle = issueTitle;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStudentPhone() {
        return studentPhone;
    }

    public void setStudentPhone(String studentPhone) {
        this.studentPhone = studentPhone;
    }

    public String getStudentEmail() {
        return studentEmail;
    }

    public void setStudentEmail(String studentEmail) {
        this.studentEmail = studentEmail;
    }

    public String getStudentName() {
        return studentName;
    }

    public void setStudentName(String studentName) {
        this.studentName = studentName;
    }

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public LocalDateTime getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(LocalDateTime submittedAt) {
        this.submittedAt = submittedAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getAcceptedBy() {
        return acceptedBy;
    }

    public void setAcceptedBy(String acceptedBy) {
        this.acceptedBy = acceptedBy;
    }

    public LocalDateTime getAcceptedAt() {
        return acceptedAt;
    }

    public void setAcceptedAt(LocalDateTime acceptedAt) {
        this.acceptedAt = acceptedAt;
    }

    public String getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(String statusCode) {
        this.statusCode = statusCode;
    }

    public String getInProgressBy() {
        return inProgressBy;
    }

    public void setInProgressBy(String inProgressBy) {
        this.inProgressBy = inProgressBy;
    }

    public LocalDateTime getInProgressAt() {
        return inProgressAt;
    }

    public void setInProgressAt(LocalDateTime inProgressAt) {
        this.inProgressAt = inProgressAt;
    }

    public LocalDateTime getVerificationRequestedAt() {
        return verificationRequestedAt;
    }

    public void setVerificationRequestedAt(LocalDateTime verificationRequestedAt) {
        this.verificationRequestedAt = verificationRequestedAt;
    }

    public String getRepairEvidenceUrl() {
        return repairEvidenceUrl;
    }

    public void setRepairEvidenceUrl(String repairEvidenceUrl) {
        this.repairEvidenceUrl = repairEvidenceUrl;
    }

    public String getRepairNotes() {
        return repairNotes;
    }

    public void setRepairNotes(String repairNotes) {
        this.repairNotes = repairNotes;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getStudentFeedback() {
        return studentFeedback;
    }

    public void setStudentFeedback(String studentFeedback) {
        this.studentFeedback = studentFeedback;
    }

    public List<String> getRepairEvidenceImages() {
        return repairEvidenceImages;
    }

    public void setRepairEvidenceImages(List<String> repairEvidenceImages) {
        this.repairEvidenceImages = repairEvidenceImages;
    }

    public String getDeclinedBy() {
        return declinedBy;
    }

    public void setDeclinedBy(String declinedBy) {
        this.declinedBy = declinedBy;
    }

    public LocalDateTime getDeclinedAt() {
        return declinedAt;
    }

    public void setDeclinedAt(LocalDateTime declinedAt) {
        this.declinedAt = declinedAt;
    }

    public String getDeclinedReason() {
        return declinedReason;
    }

    public void setDeclinedReason(String declinedReason) {
        this.declinedReason = declinedReason;
    }

    public LocalDateTime getWorkDeadline() {
        return workDeadline;
    }

    public void setWorkDeadline(LocalDateTime workDeadline) {
        this.workDeadline = workDeadline;
    }

    public boolean isEscalatedToAdmin() {
        return escalatedToAdmin;
    }

    public void setEscalatedToAdmin(boolean escalatedToAdmin) {
        this.escalatedToAdmin = escalatedToAdmin;
    }

    public LocalDateTime getEscalatedAt() {
        return escalatedAt;
    }

    public void setEscalatedAt(LocalDateTime escalatedAt) {
        this.escalatedAt = escalatedAt;
    }

    public String getEscalationReason() {
        return escalationReason;
    }

    public void setEscalationReason(String escalationReason) {
        this.escalationReason = escalationReason;
    }

    public LocalDateTime getReworkStartedAt() {
        return reworkStartedAt;
    }

    public void setReworkStartedAt(LocalDateTime reworkStartedAt) {
        this.reworkStartedAt = reworkStartedAt;
    }

    public LocalDateTime getServerTime() {
        return serverTime;
    }

    public void setServerTime(LocalDateTime serverTime) {
        this.serverTime = serverTime;
    }

    public boolean isOverdue() {
        return overdue;
    }

    public void setOverdue(boolean overdue) {
        this.overdue = overdue;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
