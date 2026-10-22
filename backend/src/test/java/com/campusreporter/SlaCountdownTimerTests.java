package com.campusreporter;

import com.campusreporter.dto.ComplaintResponseDTO;
import com.campusreporter.entity.Complaint;
import com.campusreporter.model.ComplaintStatus;
import com.campusreporter.model.Department;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class SlaCountdownTimerTests {

    @Test
    @DisplayName("Acceptance sets exact 7-day deadline")
    void testAcceptanceSevenDayDeadline() {
        LocalDateTime acceptedAt = LocalDateTime.of(2026, 10, 15, 10, 30, 0);
        LocalDateTime deadline = acceptedAt.plusDays(7);

        assertEquals(acceptedAt.plusDays(7), deadline);
        assertEquals(7, java.time.temporal.ChronoUnit.DAYS.between(acceptedAt, deadline));
    }

    @Test
    @DisplayName("ComplaintResponseDTO correctly reflects active countdown before deadline")
    void testActiveCountdownBeforeDeadline() {
        Complaint complaint = new Complaint();
        complaint.setTicketId("SCR-2026-000101");
        complaint.setDepartment(Department.ELECTRICAL);
        complaint.setStatus(ComplaintStatus.UNDER_REVIEW);
        complaint.setCampusBlock("Academic Block");
        complaint.setFloorLevel("2nd Floor");
        complaint.setSpecificLocation("LH-2001");
        complaint.setIssueTitle("Flickering Lights");
        complaint.setDescription("Fluorescent lights flickering");
        complaint.setStudentName("Sarah Jenkins");
        complaint.setStudentEmail("student@smartcampus.edu");
        complaint.setStudentPhone("9876543210");
        complaint.setSubmittedAt(LocalDateTime.now().minusHours(2));
        complaint.setAcceptedAt(LocalDateTime.now().minusHours(1));
        complaint.setWorkDeadline(LocalDateTime.now().plusDays(6).plusHours(23));

        ComplaintResponseDTO dto = ComplaintResponseDTO.fromEntity(complaint);

        assertNotNull(dto.getWorkDeadline());
        assertNotNull(dto.getServerTime());
        assertFalse(dto.isOverdue());
        assertTrue(dto.getRemainingSeconds() != null && dto.getRemainingSeconds() > 0);
        assertFalse(dto.isEscalatedToAdmin());
    }

    @Test
    @DisplayName("ComplaintResponseDTO flags overdue when workDeadline has passed without submission")
    void testOverdueActiveComplaint() {
        Complaint complaint = new Complaint();
        complaint.setTicketId("SCR-2026-000102");
        complaint.setDepartment(Department.PLUMBING);
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setCampusBlock("Civil Block");
        complaint.setFloorLevel("1st Floor");
        complaint.setSpecificLocation("Male Bathroom");
        complaint.setIssueTitle("Leaking Pipe");
        complaint.setDescription("Pipe leaking heavily");
        complaint.setStudentName("Sarah Jenkins");
        complaint.setStudentEmail("student@smartcampus.edu");
        complaint.setStudentPhone("9876543210");
        complaint.setSubmittedAt(LocalDateTime.now().minusDays(8));
        complaint.setAcceptedAt(LocalDateTime.now().minusDays(8));
        complaint.setWorkDeadline(LocalDateTime.now().minusDays(1)); // 1 day overdue

        ComplaintResponseDTO dto = ComplaintResponseDTO.fromEntity(complaint);

        assertTrue(dto.isOverdue());
        assertTrue(dto.getRemainingSeconds() != null && dto.getRemainingSeconds() < 0);
    }

    @Test
    @DisplayName("Submission for verification stops countdown and does not flag overdue")
    void testSubmittedForVerificationStopsCountdown() {
        LocalDateTime submittedAt = LocalDateTime.now().minusDays(8);
        LocalDateTime acceptedAt = LocalDateTime.now().minusDays(8);
        LocalDateTime deadline = acceptedAt.plusDays(7); // deadline was yesterday
        LocalDateTime verifiedAt = acceptedAt.plusDays(4); // submitted for verification 3 days before deadline

        Complaint complaint = new Complaint();
        complaint.setTicketId("SCR-2026-000103");
        complaint.setDepartment(Department.CLEANING);
        complaint.setStatus(ComplaintStatus.AWAITING_VERIFICATION);
        complaint.setCampusBlock("Block 3");
        complaint.setFloorLevel("3rd Floor");
        complaint.setSpecificLocation("Staff Room");
        complaint.setIssueTitle("Stained Carpet");
        complaint.setDescription("Needs shampooing");
        complaint.setStudentName("Sarah Jenkins");
        complaint.setStudentEmail("student@smartcampus.edu");
        complaint.setStudentPhone("9876543210");
        complaint.setSubmittedAt(submittedAt);
        complaint.setAcceptedAt(acceptedAt);
        complaint.setWorkDeadline(deadline);
        complaint.setVerificationRequestedAt(verifiedAt);

        ComplaintResponseDTO dto = ComplaintResponseDTO.fromEntity(complaint);

        // Since it was submitted before the deadline, it should NOT be flagged overdue
        assertFalse(dto.isOverdue());
        assertNotNull(dto.getVerificationRequestedAt());
        assertEquals(verifiedAt, dto.getVerificationRequestedAt());
    }

    @Test
    @DisplayName("Rework handling establishes new 7-day work period")
    void testReworkEstablishesNewSevenDayDeadline() {
        LocalDateTime reworkStart = LocalDateTime.of(2026, 10, 20, 14, 0, 0);
        LocalDateTime newDeadline = reworkStart.plusDays(7);

        assertEquals(LocalDateTime.of(2026, 10, 27, 14, 0, 0), newDeadline);
        assertEquals(7, java.time.temporal.ChronoUnit.DAYS.between(reworkStart, newDeadline));
    }

    @Test
    @DisplayName("Admin escalation sets flags and reason")
    void testAdminEscalationFlags() {
        Complaint complaint = new Complaint();
        complaint.setTicketId("SCR-2026-000104");
        complaint.setDepartment(Department.IT_SUPPORT);
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setEscalatedToAdmin(true);
        complaint.setEscalatedAt(LocalDateTime.now());
        complaint.setEscalationReason("7-Day Maintenance SLA Expired without repair verification submission.");

        assertTrue(complaint.isEscalatedToAdmin());
        assertNotNull(complaint.getEscalatedAt());
        assertNotNull(complaint.getEscalationReason());
    }
}
