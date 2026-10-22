package com.campusreporter;

import com.campusreporter.dto.ComplaintRequestDTO;
import com.campusreporter.dto.ComplaintResponseDTO;
import com.campusreporter.entity.Complaint;
import com.campusreporter.exception.UnauthorizedException;
import com.campusreporter.exception.ValidationException;
import com.campusreporter.model.ComplaintStatus;
import com.campusreporter.model.Department;
import com.campusreporter.repository.ComplaintAuditLogRepository;
import com.campusreporter.repository.ComplaintRepository;
import com.campusreporter.service.FileStorageService;
import com.campusreporter.service.ComplaintServiceImpl;
import com.campusreporter.service.TicketIdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class ComplaintImageTests {

    private FileStorageService fileStorageService;
    private ComplaintRepository complaintRepository;
    private ComplaintAuditLogRepository auditLogRepository;
    private TicketIdGenerator ticketIdGenerator;
    private ComplaintServiceImpl complaintService;

    @BeforeEach
    void setUp() {
        fileStorageService = new FileStorageService();
        complaintRepository = mock(ComplaintRepository.class);
        auditLogRepository = mock(ComplaintAuditLogRepository.class);
        ticketIdGenerator = mock(TicketIdGenerator.class);
        complaintService = new ComplaintServiceImpl(
            complaintRepository,
            auditLogRepository,
            ticketIdGenerator,
            fileStorageService
        );
    }

    @Test
    @DisplayName("FileStorageService correctly stores valid complaint images and returns secure URLs")
    void testStoreComplaintImages_Success() throws IOException {
        String ticketId = "SCR-2026-000099";
        MockMultipartFile file1 = new MockMultipartFile(
            "files", "broken_tap.jpg", "image/jpeg", "fake-jpeg-data".getBytes()
        );
        MockMultipartFile file2 = new MockMultipartFile(
            "files", "water_leak.png", "image/png", "fake-png-data".getBytes()
        );

        List<String> urls = fileStorageService.storeComplaintImages(ticketId, List.of(file1, file2));

        assertNotNull(urls);
        assertEquals(2, urls.size());
        assertTrue(urls.get(0).startsWith("/api/complaints/SCR-2026-000099/images/"));
        assertTrue(urls.get(0).endsWith(".jpg"));
        assertTrue(urls.get(1).startsWith("/api/complaints/SCR-2026-000099/images/"));
        assertTrue(urls.get(1).endsWith(".png"));

        // Verify the file was created and can be loaded via loadComplaintFile
        String filename1 = urls.get(0).substring(urls.get(0).lastIndexOf('/') + 1);
        Resource loaded = fileStorageService.loadComplaintFile(filename1);
        assertNotNull(loaded);
        assertTrue(loaded.exists());
    }

    @Test
    @DisplayName("FileStorageService enforces the 5 initial images limit")
    void testStoreComplaintImages_MaxLimitExceeded() {
        String ticketId = "SCR-2026-000100";
        List<MultipartFile> files = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            files.add(new MockMultipartFile(
                "files", "photo" + i + ".jpg", "image/jpeg", "image-content".getBytes()
            ));
        }

        ValidationException ex = assertThrows(ValidationException.class, () ->
            fileStorageService.storeComplaintImages(ticketId, files)
        );
        assertTrue(ex.getMessage().contains("Maximum of 5 complaint images are permitted"));
    }

    @Test
    @DisplayName("FileStorageService rejects disallowed file extensions")
    void testStoreComplaintImages_InvalidExtension() {
        String ticketId = "SCR-2026-000101";
        MockMultipartFile executable = new MockMultipartFile(
            "files", "script.exe", "application/octet-stream", "malicious-code".getBytes()
        );

        ValidationException ex = assertThrows(ValidationException.class, () ->
            fileStorageService.storeComplaintImages(ticketId, List.of(executable))
        );
        assertTrue(ex.getMessage().contains("Invalid image format"));
    }

    @Test
    @DisplayName("FileStorageService blocks path traversal attempts")
    void testLoadComplaintFile_PathTraversalBlocked() {
        assertNull(fileStorageService.loadComplaintFile("../../../etc/passwd"));
        assertNull(fileStorageService.loadComplaintFile("..\\..\\windows\\system32"));
        assertNull(fileStorageService.loadComplaintFile("subdir/something.jpg"));
        assertNull(fileStorageService.loadComplaintFile(""));
        assertNull(fileStorageService.loadComplaintFile(null));
    }

    @Test
    @DisplayName("FileStorageService returns null for non-existent image files")
    void testLoadComplaintFile_NotFound() {
        assertNull(fileStorageService.loadComplaintFile("complaint-scr-2026-nonexistent-999.jpg"));
    }

    @Test
    @DisplayName("getComplaintByTicketId enforces student privacy")
    void testAuthorization_StudentPrivacy() {
        Complaint complaint = new Complaint();
        complaint.setTicketId("SCR-2026-000058");
        complaint.setStudentEmail("student1@smartcampus.edu");
        complaint.setDepartment(Department.CLEANING);
        complaint.setStatus(ComplaintStatus.SUBMITTED);

        when(complaintRepository.findByTicketId("SCR-2026-000058")).thenReturn(Optional.of(complaint));

        // Allowed: Owner student
        ComplaintResponseDTO dto = complaintService.getComplaintByTicketId(
            "SCR-2026-000058", "student1@smartcampus.edu", "STUDENT"
        );
        assertNotNull(dto);
        assertEquals("SCR-2026-000058", dto.getTicketId());

        // Denied: Different student
        assertThrows(UnauthorizedException.class, () ->
            complaintService.getComplaintByTicketId(
                "SCR-2026-000058", "student2@smartcampus.edu", "STUDENT"
            )
        );
    }

    @Test
    @DisplayName("getComplaintByTicketId enforces department segregation")
    void testAuthorization_DepartmentSegregation() {
        Complaint complaint = new Complaint();
        complaint.setTicketId("SCR-2026-000058");
        complaint.setStudentEmail("student1@smartcampus.edu");
        complaint.setDepartment(Department.CLEANING);
        complaint.setStatus(ComplaintStatus.SUBMITTED);

        when(complaintRepository.findByTicketId("SCR-2026-000058")).thenReturn(Optional.of(complaint));

        // Allowed: Cleaning staff viewing Cleaning complaint
        ComplaintResponseDTO dtoCleaning = complaintService.getComplaintByTicketId(
            "SCR-2026-000058", "cleaning_lead@smartcampus.edu", "CLEANING"
        );
        assertNotNull(dtoCleaning);

        // Allowed: Admin viewing Cleaning complaint
        ComplaintResponseDTO dtoAdmin = complaintService.getComplaintByTicketId(
            "SCR-2026-000058", "admin@smartcampus.edu", "ADMIN"
        );
        assertNotNull(dtoAdmin);

        // Denied: Electrical staff viewing Cleaning complaint
        assertThrows(UnauthorizedException.class, () ->
            complaintService.getComplaintByTicketId(
                "SCR-2026-000058", "electrical_tech@smartcampus.edu", "ELECTRICAL"
            )
        );

        // Denied: Unauthenticated caller
        assertThrows(UnauthorizedException.class, () ->
            complaintService.getComplaintByTicketId("SCR-2026-000058", null, null)
        );
    }

    @Test
    @DisplayName("submitComplaint persists uploaded images correctly and links them to the ticket")
    void testSubmitComplaint_WithImages_PersistsCorrectly() {
        when(ticketIdGenerator.generateTicketId()).thenReturn("SCR-2026-000077");
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ComplaintRequestDTO request = new ComplaintRequestDTO();
        request.setDepartment("CLEANING");
        request.setCampusBlock("Mechanical Block");
        request.setFloorLevel("2nd Floor");
        request.setSpecificLocation("LH-2008");
        request.setIssueTitle("Spilled chemicals on floor");
        request.setDescription("Severe chemical spill needs immediate industrial cleaning.");
        request.setStudentPhone("7894561230");
        request.setEmergency(true);

        MockMultipartFile image1 = new MockMultipartFile(
            "files", "spill1.jpg", "image/jpeg", "spill-photo-1".getBytes()
        );
        MockMultipartFile image2 = new MockMultipartFile(
            "files", "spill2.png", "image/png", "spill-photo-2".getBytes()
        );

        ComplaintResponseDTO response = complaintService.submitComplaint(
            request,
            new MultipartFile[]{image1, image2},
            "student@smartcampus.edu",
            "STUDENT"
        );

        assertNotNull(response);
        assertEquals("SCR-2026-000077", response.getTicketId());
        assertNotNull(response.getImageUrls());
        assertEquals(2, response.getImageUrls().size());
        assertTrue(response.getImageUrls().get(0).contains("SCR-2026-000077"));
        assertTrue(response.getImageUrls().get(1).contains("SCR-2026-000077"));
    }
}
