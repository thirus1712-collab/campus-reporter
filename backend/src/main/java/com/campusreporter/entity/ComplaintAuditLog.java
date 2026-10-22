package com.campusreporter.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "complaint_audit_logs",
    indexes = {
        @Index(name = "idx_audit_complaint_id", columnList = "complaint_id"),
        @Index(name = "idx_audit_ticket_id", columnList = "ticket_id"),
        @Index(name = "idx_audit_timestamp", columnList = "action_timestamp")
    }
)
public class ComplaintAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "complaint_id", nullable = false)
    private Long complaintId;

    @Column(name = "ticket_id", nullable = false, length = 32)
    private String ticketId;

    @Column(name = "action", nullable = false, length = 50)
    private String action;

    @Column(name = "previous_status", length = 32)
    private String previousStatus;

    @Column(name = "new_status", length = 32)
    private String newStatus;

    @Column(name = "performed_by", nullable = false, length = 120)
    private String performedBy;

    @Column(name = "notes", length = 500)
    private String notes;

    @Column(name = "action_timestamp", nullable = false)
    private LocalDateTime actionTimestamp;

    @PrePersist
    public void onPrePersist() {
        if (this.actionTimestamp == null) {
            this.actionTimestamp = LocalDateTime.now();
        }
    }

    public ComplaintAuditLog() {}

    public ComplaintAuditLog(Long complaintId, String ticketId, String action, String previousStatus, String newStatus, String performedBy, String notes) {
        this.complaintId = complaintId;
        this.ticketId = ticketId;
        this.action = action;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.performedBy = performedBy;
        this.notes = notes;
        this.actionTimestamp = LocalDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getComplaintId() { return complaintId; }
    public void setComplaintId(Long complaintId) { this.complaintId = complaintId; }
    public String getTicketId() { return ticketId; }
    public void setTicketId(String ticketId) { this.ticketId = ticketId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getPreviousStatus() { return previousStatus; }
    public void setPreviousStatus(String previousStatus) { this.previousStatus = previousStatus; }
    public String getNewStatus() { return newStatus; }
    public void setNewStatus(String newStatus) { this.newStatus = newStatus; }
    public String getPerformedBy() { return performedBy; }
    public void setPerformedBy(String performedBy) { this.performedBy = performedBy; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public LocalDateTime getActionTimestamp() { return actionTimestamp; }
    public void setActionTimestamp(LocalDateTime actionTimestamp) { this.actionTimestamp = actionTimestamp; }
}
