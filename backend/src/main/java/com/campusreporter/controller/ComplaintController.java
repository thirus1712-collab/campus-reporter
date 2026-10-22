package com.campusreporter.controller;

import com.campusreporter.dto.ComplaintRequestDTO;
import com.campusreporter.dto.ComplaintResponseDTO;
import com.campusreporter.dto.DepartmentActionDTO;
import com.campusreporter.entity.User;
import com.campusreporter.service.AuthService;
import com.campusreporter.service.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import com.campusreporter.entity.Complaint;
import com.campusreporter.model.Department;
import com.campusreporter.repository.ComplaintRepository;
import com.campusreporter.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/complaints")
public class ComplaintController {

    private final ComplaintService complaintService;
    private final AuthService authService;
    private final FileStorageService fileStorageService;
    private final ComplaintRepository complaintRepository;

    @Autowired
    public ComplaintController(
        ComplaintService complaintService,
        AuthService authService,
        FileStorageService fileStorageService,
        ComplaintRepository complaintRepository
    ) {
        this.complaintService = complaintService;
        this.authService = authService;
        this.fileStorageService = fileStorageService;
        this.complaintRepository = complaintRepository;
    }

    private User resolveUser(String authHeader, String fallbackEmail, String fallbackRole) {
        return resolveUser(authHeader, null, fallbackEmail, fallbackRole);
    }

    private User resolveUser(String authHeader, String queryToken, String fallbackEmail, String fallbackRole) {
        if (authHeader != null && !authHeader.trim().isEmpty()) {
            return authService.validateToken(authHeader);
        }
        if (queryToken != null && !queryToken.trim().isEmpty()) {
            return authService.validateToken(queryToken);
        }
        return null;
    }

    /**
     * POST /api/complaints (multipart/form-data)
     * Submit a new complaint with up to 5 attached photos.
     */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintResponseDTO> submitComplaintMultipart(
        @Valid @ModelAttribute ComplaintRequestDTO request,
        @RequestParam(value = "files", required = false) MultipartFile[] files,
        @RequestParam(value = "images", required = false) MultipartFile[] images,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, userEmail, userRole);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        MultipartFile[] effectiveFiles = (files != null && files.length > 0) ? files : images;
        ComplaintResponseDTO response = complaintService.submitComplaint(request, effectiveFiles, resolvedEmail, resolvedRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/complaints (application/json)
     * Submit a new complaint via JSON.
     */
    @PostMapping(consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.ALL_VALUE})
    public ResponseEntity<ComplaintResponseDTO> submitComplaintJson(
        @Valid @RequestBody ComplaintRequestDTO request,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, userEmail, userRole);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        ComplaintResponseDTO response = complaintService.submitComplaint(request, null, resolvedEmail, resolvedRole);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * GET /api/complaints/mine
     * Retrieve the complaints filed by the authenticated student.
     */
    @GetMapping("/mine")
    public ResponseEntity<List<ComplaintResponseDTO>> getMyComplaints(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        User user = resolveUser(authHeader, userEmail, null);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;

        List<ComplaintResponseDTO> list = complaintService.getMyComplaints(resolvedEmail);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/complaints/department
     * Retrieve complaints assigned to a department.
     */
    @GetMapping("/department")
    public ResponseEntity<List<ComplaintResponseDTO>> getDepartmentComplaints(
        @RequestParam(value = "dept", defaultValue = "ELECTRICAL") String dept,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, null, userRole);
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        List<ComplaintResponseDTO> list = complaintService.getDepartmentComplaints(dept, resolvedRole);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/complaints/all
     * Retrieve all campus complaints for Admin oversight with optional status filtering.
     */
    @GetMapping("/all")
    public ResponseEntity<List<ComplaintResponseDTO>> getAllComplaints(
        @RequestParam(value = "status", required = false) String status,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        List<ComplaintResponseDTO> list = complaintService.getAllComplaintsAdmin(status);
        return ResponseEntity.ok(list);
    }

    /**
     * GET /api/complaints/{ticketId}
     * Retrieve single complaint details by Ticket ID.
     */
    @GetMapping("/{ticketId}")
    public ResponseEntity<ComplaintResponseDTO> getComplaint(
        @PathVariable("ticketId") String ticketId,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, userEmail, userRole);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        ComplaintResponseDTO response = complaintService.getComplaintByTicketId(ticketId, resolvedEmail, resolvedRole);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/complaints/{ticketId}/images/{filename:.+}
     * Securely serves student-uploaded complaint images to authorized users.
     * Enforces RBAC:
     * - Students can only access images of their own tickets.
     * - Department staff can only access images of tickets assigned to their department.
     * - Admins can access all tickets.
     * - Unauthenticated or unauthorized access is denied.
     */
    @GetMapping("/{ticketId}/images/{filename:.+}")
    public ResponseEntity<Resource> getComplaintImage(
        @PathVariable("ticketId") String ticketId,
        @PathVariable("filename") String filename,
        @RequestParam(value = "token", required = false) String queryToken,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, queryToken, userEmail, userRole);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        if (resolvedRole == null || resolvedRole.trim().isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        // Validate ticket and authorize caller
        Complaint complaint = complaintRepository.findByTicketId(ticketId.trim()).orElse(null);
        if (complaint == null) {
            return ResponseEntity.notFound().build();
        }

        // Authorization check: Student can only view their own complaint's images
        if (resolvedRole.equalsIgnoreCase("STUDENT")) {
            if (resolvedEmail == null || !complaint.getStudentEmail().equalsIgnoreCase(resolvedEmail.trim())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // Authorization check: Department staff can only view images for their assigned department
        if (!resolvedRole.equalsIgnoreCase("ADMIN") && !resolvedRole.equalsIgnoreCase("STUDENT")) {
            Department staffDept = Department.fromString(resolvedRole);
            if (staffDept != null && staffDept != complaint.getDepartment()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }

        // Path traversal / sanitization check
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        // Verify that this image is actually registered to this ticket
        boolean isRegistered = complaint.getImageUrls() != null && complaint.getImageUrls().stream()
            .anyMatch(url -> url != null && url.contains(filename));
        if (!isRegistered) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = fileStorageService.loadComplaintFile(filename);
        if (resource == null || !resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String lower = filename.toLowerCase();
        String contentType = "image/jpeg";
        if (lower.endsWith(".png")) {
            contentType = "image/png";
        } else if (lower.endsWith(".webp")) {
            contentType = "image/webp";
        }

        return ResponseEntity.ok()
            .contentType(MediaType.parseMediaType(contentType))
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
            .header(HttpHeaders.CACHE_CONTROL, "private, max-age=3600")
            .body(resource);
    }

    /**
     * PATCH or POST /api/complaints/{ticketId}/accept
     * Department accepts complaint -> Under Review.
     */
    @RequestMapping(value = "/{ticketId}/accept", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ComplaintResponseDTO> acceptComplaint(
        @PathVariable("ticketId") String ticketId,
        @RequestBody(required = false) DepartmentActionDTO action,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, userEmail, userRole);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        ComplaintResponseDTO updated = complaintService.acceptComplaint(ticketId, action, resolvedEmail, resolvedRole);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH or POST /api/complaints/{ticketId}/decline
     * Department declines complaint with a required reason -> Declined.
     */
    @RequestMapping(value = "/{ticketId}/decline", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ComplaintResponseDTO> declineComplaint(
        @PathVariable("ticketId") String ticketId,
        @RequestBody DepartmentActionDTO action,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, userEmail, userRole);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        ComplaintResponseDTO updated = complaintService.declineComplaint(ticketId, action, resolvedEmail, resolvedRole);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH or POST /api/complaints/{ticketId}/progress (and /start)
     * Department begins physical repair work -> In Progress.
     */
    @RequestMapping(value = {"/{ticketId}/progress", "/{ticketId}/start"}, method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ComplaintResponseDTO> startWork(
        @PathVariable("ticketId") String ticketId,
        @RequestBody(required = false) DepartmentActionDTO action,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, userEmail, userRole);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        ComplaintResponseDTO updated = complaintService.startWork(ticketId, action, resolvedEmail, resolvedRole);
        return ResponseEntity.ok(updated);
    }

    /**
     * POST /api/complaints/{ticketId}/complete
     * Department completes physical work with 1-5 proof images and optional acknowledgement.
     */
    @PostMapping(value = "/{ticketId}/complete", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ComplaintResponseDTO> completeWorkWithEvidence(
        @PathVariable("ticketId") String ticketId,
        @RequestParam(value = "files", required = false) MultipartFile[] files,
        @RequestParam(value = "acknowledgement", required = false) String acknowledgement,
        @RequestParam(value = "staffName", required = false) String staffName,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, userEmail, userRole);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        ComplaintResponseDTO updated = complaintService.completeWorkWithEvidence(
            ticketId, files, acknowledgement, staffName, resolvedEmail, resolvedRole
        );
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH or POST /api/complaints/{ticketId}/evidence
     * Technician submits repair evidence photos -> Awaiting Student Verification.
     */
    @RequestMapping(value = "/{ticketId}/evidence", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ComplaintResponseDTO> submitEvidence(
        @PathVariable("ticketId") String ticketId,
        @RequestBody(required = false) DepartmentActionDTO action,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, userEmail, userRole);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        ComplaintResponseDTO updated = complaintService.submitEvidence(ticketId, action, resolvedEmail, resolvedRole);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH or POST /api/complaints/{ticketId}/verify
     * Student confirms repair and closes ticket -> Closed.
     */
    @RequestMapping(value = "/{ticketId}/verify", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ComplaintResponseDTO> verifyResolution(
        @PathVariable("ticketId") String ticketId,
        @RequestBody(required = false) DepartmentActionDTO action,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        User user = resolveUser(authHeader, userEmail, null);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;

        ComplaintResponseDTO updated = complaintService.verifyResolution(ticketId, action, resolvedEmail);
        return ResponseEntity.ok(updated);
    }

    /**
     * PATCH or POST /api/complaints/{ticketId}/dispute
     * Student rejects repair evidence and requests rework -> In Progress.
     */
    @RequestMapping(value = "/{ticketId}/dispute", method = {RequestMethod.PATCH, RequestMethod.POST})
    public ResponseEntity<ComplaintResponseDTO> disputeResolution(
        @PathVariable("ticketId") String ticketId,
        @RequestBody(required = false) DepartmentActionDTO action,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        User user = resolveUser(authHeader, userEmail, null);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;

        ComplaintResponseDTO updated = complaintService.disputeResolution(ticketId, action, resolvedEmail);
        return ResponseEntity.ok(updated);
    }

    /**
     * GET /api/complaints/metrics/student
     */
    @GetMapping("/metrics/student")
    public ResponseEntity<Map<String, Long>> getStudentMetrics(
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Email", required = false) String userEmail
    ) {
        User user = resolveUser(authHeader, userEmail, null);
        String resolvedEmail = user != null ? user.getEmail() : userEmail;

        Map<String, Long> metrics = complaintService.getStudentMetrics(resolvedEmail);
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/complaints/metrics/department
     */
    @GetMapping("/metrics/department")
    public ResponseEntity<Map<String, Long>> getDepartmentMetrics(
        @RequestParam(value = "dept", defaultValue = "ELECTRICAL") String dept,
        @RequestHeader(value = "Authorization", required = false) String authHeader,
        @RequestHeader(value = "X-User-Role", required = false) String userRole
    ) {
        User user = resolveUser(authHeader, null, userRole);
        String resolvedRole = user != null ? user.getRole().name() : userRole;

        Map<String, Long> metrics = complaintService.getDepartmentMetrics(dept, resolvedRole);
        return ResponseEntity.ok(metrics);
    }

    /**
     * GET /api/complaints/metrics/admin
     */
    @GetMapping("/metrics/admin")
    public ResponseEntity<Map<String, Object>> getAdminMetrics() {
        Map<String, Object> metrics = complaintService.getAdminMetrics();
        return ResponseEntity.ok(metrics);
    }
}
