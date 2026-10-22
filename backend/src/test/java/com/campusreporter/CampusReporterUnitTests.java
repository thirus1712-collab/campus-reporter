package com.campusreporter;

import com.campusreporter.model.CampusLocations;
import com.campusreporter.model.ComplaintStatus;
import com.campusreporter.model.Department;
import com.campusreporter.model.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

class CampusReporterUnitTests {

    @Test
    @DisplayName("CampusLocations validates correct blocks")
    void testValidBlocks() {
        assertTrue(CampusLocations.isValidBlock("Academic Block"));
        assertTrue(CampusLocations.isValidBlock("Civil Block"));
        assertTrue(CampusLocations.isValidBlock("Mechanical Block"));
        assertTrue(CampusLocations.isValidBlock("Block 3"));
        assertTrue(CampusLocations.isValidBlock("Electronics Block"));

        assertFalse(CampusLocations.isValidBlock("Library Block"));
        assertFalse(CampusLocations.isValidBlock(""));
        assertFalse(CampusLocations.isValidBlock(null));
    }

    @Test
    @DisplayName("CampusLocations validates correct floors")
    void testValidFloors() {
        assertTrue(CampusLocations.isValidFloor("1st Floor"));
        assertTrue(CampusLocations.isValidFloor("2nd Floor"));
        assertTrue(CampusLocations.isValidFloor("3rd Floor"));
        assertTrue(CampusLocations.isValidFloor("4th Floor"));

        assertFalse(CampusLocations.isValidFloor("Ground Floor"));
        assertFalse(CampusLocations.isValidFloor("5th Floor"));
        assertFalse(CampusLocations.isValidFloor(null));
    }

    @Test
    @DisplayName("CampusLocations validates classroom ranges and shared locations")
    void testValidLocations() {
        // Classrooms on 1st Floor: LH-1001 through LH-1010
        assertTrue(CampusLocations.isValidLocation("1st Floor", "LH-1001"));
        assertTrue(CampusLocations.isValidLocation("1st Floor", "LH-1010"));
        assertFalse(CampusLocations.isValidLocation("1st Floor", "LH-1011"));
        assertFalse(CampusLocations.isValidLocation("1st Floor", "LH-2001"));

        // Classrooms on 2nd Floor: LH-2001 through LH-2010
        assertTrue(CampusLocations.isValidLocation("2nd Floor", "LH-2005"));
        assertFalse(CampusLocations.isValidLocation("2nd Floor", "LH-1005"));

        // Shared locations on every floor
        assertTrue(CampusLocations.isValidLocation("1st Floor", "Male Bathroom"));
        assertTrue(CampusLocations.isValidLocation("2nd Floor", "Female Bathroom"));
        assertTrue(CampusLocations.isValidLocation("3rd Floor", "Staff Room"));

        assertFalse(CampusLocations.isValidLocation("1st Floor", "Principal Office"));
        assertFalse(CampusLocations.isValidLocation(null, "LH-1001"));
        assertFalse(CampusLocations.isValidLocation("1st Floor", null));
    }

    @Test
    @DisplayName("Department enum matches all four required maintenance domains")
    void testDepartmentEnum() {
        assertEquals("ELECTRICAL", Department.ELECTRICAL.name());
        assertEquals("PLUMBING", Department.PLUMBING.name());
        assertEquals("CLEANING", Department.CLEANING.name());
        assertEquals("IT_SUPPORT", Department.IT_SUPPORT.name());

        assertEquals("Electrical", Department.ELECTRICAL.getDisplayName());
        assertEquals("Plumbing", Department.PLUMBING.getDisplayName());
        assertEquals("Cleaning", Department.CLEANING.getDisplayName());
        assertEquals("IT & Technical Support", Department.IT_SUPPORT.getDisplayName());
    }

    @Test
    @DisplayName("ComplaintStatus transitions represent proper lifecycle states")
    void testComplaintStatus() {
        assertEquals("Submitted", ComplaintStatus.SUBMITTED.getDisplayName());
        assertEquals("Under Review", ComplaintStatus.UNDER_REVIEW.getDisplayName());
        assertEquals("In Progress", ComplaintStatus.IN_PROGRESS.getDisplayName());
        assertEquals("Awaiting Student Verification", ComplaintStatus.AWAITING_VERIFICATION.getDisplayName());
        assertEquals("Closed", ComplaintStatus.CLOSED.getDisplayName());
        assertEquals("Declined", ComplaintStatus.DECLINED.getDisplayName());
    }

    @Test
    @DisplayName("UserRole enum matches all six authorized roles with valid portal URLs")
    void testUserRoles() {
        assertEquals("STUDENT", UserRole.STUDENT.name());
        assertEquals("ELECTRICAL", UserRole.ELECTRICAL.name());
        assertEquals("PLUMBING", UserRole.PLUMBING.name());
        assertEquals("CLEANING", UserRole.CLEANING.name());
        assertEquals("IT_SUPPORT", UserRole.IT_SUPPORT.name());
        assertEquals("ADMIN", UserRole.ADMIN.name());

        assertEquals("student/index.html", UserRole.STUDENT.getDefaultPortalUrl());
        assertEquals("department/index.html?dept=ELECTRICAL", UserRole.ELECTRICAL.getDefaultPortalUrl());
        assertEquals("department/index.html?dept=PLUMBING", UserRole.PLUMBING.getDefaultPortalUrl());
        assertEquals("department/index.html?dept=CLEANING", UserRole.CLEANING.getDefaultPortalUrl());
        assertEquals("department/index.html?dept=IT_SUPPORT", UserRole.IT_SUPPORT.getDefaultPortalUrl());
        assertEquals("admin/index.html", UserRole.ADMIN.getDefaultPortalUrl());
    }

    @Test
    @DisplayName("Ticket ID matches SCR-YYYY-XXXXXX pattern")
    void testTicketIdFormat() {
        Pattern pattern = Pattern.compile("^SCR-\\d{4}-\\d{6}$");
        assertTrue(pattern.matcher("SCR-2026-000001").matches());
        assertTrue(pattern.matcher("SCR-2026-000021").matches());
        assertTrue(pattern.matcher("SCR-2026-999999").matches());
        assertFalse(pattern.matcher("SCR-26-000001").matches());
        assertFalse(pattern.matcher("TICKET-123").matches());
    }

    @Test
    @DisplayName("BCrypt password encoder securely encodes and verifies passwords")
    void testPasswordHashing() {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String raw = "Student@123";
        String encoded = encoder.encode(raw);

        assertNotEquals(raw, encoded);
        assertTrue(encoder.matches(raw, encoded));
        assertFalse(encoder.matches("WrongPassword", encoded));
    }
}
