package com.campusreporter.entity;

import com.campusreporter.model.ComplaintStatus;
import com.campusreporter.model.Department;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "complaints",
    indexes = {
        @Index(name = "idx_complaint_ticket_id", columnList = "ticket_id", unique = true),
        @Index(name = "idx_complaint_student_email", columnList = "student_email"),
        @Index(name = "idx_complaint_department", columnList = "department"),
        @Index(name = "idx_complaint_status", columnList = "status"),
        @Index(name = "idx_complaint_submitted_at", columnList = "submitted_at")
    }
)
public class Complaint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ticket_id", nullable = false, unique = true, length = 32)
    private String ticketId;

    @Column(name = "student_email", nullable = false, length = 120)
    private String studentEmail;

    @Column(name = "student_name", length = 100)
    private String studentName;

    @Enumerated(EnumType.STRING)
    @Column(name = "department", nullable = false, length = 32)
    private Department department;

    @Column(name = "campus_block", nullable = false, length = 64)
    private String campusBlock;

    @Column(name = "floor_level", nullable = false, length = 32)
    private String floorLevel;

    @Column(name = "specific_location", nullable = false, length = 64)
    private String specificLocation;

    @Column(name = "issue_title", nullable = false, length = 150)
    private String issueTitle;

    @Column(name = "description", nullable = false, length = 1500)
    private String description;

    @Column(name = "student_phone", nullable = false, length = 20)
    private String studentPhone;

    @Column(name = "is_emergency", nullable = false)
    private boolean emergency;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 32)
    private ComplaintStatus status = ComplaintStatus.SUBMITTED;

    @Column(name = "idempotency_key", length = 128)
    private String idempotencyKey;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "complaint_images", joinColumns = @JoinColumn(name = "complaint_id"))
    @Column(name = "image_url")
    private List<String> imageUrls = new ArrayList<>();

    @Column(name = "accepted_by", length = 100)
    private String acceptedBy;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "declined_by", length = 100)
    private String declinedBy;

    @Column(name = "declined_reason", length = 500)
    private String declinedReason;

    @Column(name = "declined_at")
    private LocalDateTime declinedAt;

    @Column(name = "in_progress_by", length = 100)
    private String inProgressBy;

    @Column(name = "in_progress_at")
    private LocalDateTime inProgressAt;

    @Column(name = "verification_requested_at")
    private LocalDateTime verificationRequestedAt;

    @Column(name = "repair_evidence_url", length = 500)
    private String repairEvidenceUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "complaint_evidence_images", joinColumns = @JoinColumn(name = "complaint_id"))
    @Column(name = "image_url")
    private List<String> repairEvidenceImages = new ArrayList<>();

    @Column(name = "repair_notes", length = 1000)
    private String repairNotes;

    @Column(name = "resolved_by", length = 100)
    private String resolvedBy;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "student_feedback", length = 500)
    private String studentFeedback;

    @Column(name = "work_deadline")
    private LocalDateTime workDeadline;

    @Column(name = "escalated_to_admin", columnDefinition = "boolean default false")
    private boolean escalatedToAdmin = false;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalation_reason", length = 500)
    private String escalationReason;

    @Column(name = "rework_started_at")
    private LocalDateTime reworkStartedAt;

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void onPrePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (this.submittedAt == null) {
            this.submittedAt = now;
        }
        if (this.updatedAt == null) {
            this.updatedAt = now;
        }
        if (this.status == null) {
            this.status = ComplaintStatus.SUBMITTED;
        }
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Complaint() {}

    // Getters and Setters

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

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public String getCampusBlock() {
        return campusBlock;
    }

    public void setCampusBlock(String campusBlock) {
        this.campusBlock = campusBlock;
    }

    public String getFloorLevel() {
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

    public boolean isEmergency() {
        return emergency;
    }

    public void setEmergency(boolean emergency) {
        this.emergency = emergency;
    }

    public ComplaintStatus getStatus() {
        return status;
    }

    public void setStatus(ComplaintStatus status) {
        this.status = status;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(String idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
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

    public String getDeclinedBy() {
        return declinedBy;
    }

    public void setDeclinedBy(String declinedBy) {
        this.declinedBy = declinedBy;
    }

    public String getDeclinedReason() {
        return declinedReason;
    }

    public void setDeclinedReason(String declinedReason) {
        this.declinedReason = declinedReason;
    }

    public LocalDateTime getDeclinedAt() {
        return declinedAt;
    }

    public void setDeclinedAt(LocalDateTime declinedAt) {
        this.declinedAt = declinedAt;
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
        if ((repairEvidenceUrl == null || repairEvidenceUrl.trim().isEmpty()) && repairEvidenceImages != null && !repairEvidenceImages.isEmpty()) {
            return repairEvidenceImages.get(0);
        }
        return repairEvidenceUrl;
    }

    public void setRepairEvidenceUrl(String repairEvidenceUrl) {
        this.repairEvidenceUrl = repairEvidenceUrl;
        if (repairEvidenceUrl != null && !repairEvidenceUrl.trim().isEmpty()) {
            if (this.repairEvidenceImages == null) {
                this.repairEvidenceImages = new ArrayList<>();
            }
            if (!this.repairEvidenceImages.contains(repairEvidenceUrl.trim())) {
                this.repairEvidenceImages.add(repairEvidenceUrl.trim());
            }
        }
    }

    public List<String> getRepairEvidenceImages() {
        if (repairEvidenceImages == null || repairEvidenceImages.isEmpty()) {
            if (repairEvidenceUrl != null && !repairEvidenceUrl.trim().isEmpty()) {
                return List.of(repairEvidenceUrl.trim());
            }
            return new ArrayList<>();
        }
        return repairEvidenceImages;
    }

    public void setRepairEvidenceImages(List<String> repairEvidenceImages) {
        this.repairEvidenceImages = repairEvidenceImages != null ? new ArrayList<>(repairEvidenceImages) : new ArrayList<>();
        if (!this.repairEvidenceImages.isEmpty()) {
            this.repairEvidenceUrl = this.repairEvidenceImages.get(0);
        }
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
}
