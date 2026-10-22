package com.campusreporter.controller;

import com.campusreporter.entity.Complaint;
import com.campusreporter.entity.User;
import com.campusreporter.model.Department;
import com.campusreporter.repository.ComplaintRepository;
import com.campusreporter.service.AuthService;
import com.campusreporter.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;
    private final AuthService authService;
    private final ComplaintRepository complaintRepository;

    public FileController(FileStorageService fileStorageService, AuthService authService, ComplaintRepository complaintRepository) {
        this.fileStorageService = fileStorageService;
        this.authService = authService;
        this.complaintRepository = complaintRepository;
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
     * GET /api/files/evidence/{filename:.+}
     * Serves repair proof photos stored on disk to authorized users.
     */
    @GetMapping("/evidence/{filename:.+}")
    public ResponseEntity<Resource> getEvidenceFile(
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

        // Path traversal protection
        if (filename == null || filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        // Find associated complaint by parsing ticket ID from filename evidence-<ticketId>-...
        if (filename.startsWith("evidence-")) {
            String remainder = filename.substring(9);
            int nextDash = remainder.indexOf('-', remainder.indexOf('-', remainder.indexOf('-') + 1) + 1);
            String ticketIdCandidate = nextDash > 0 ? remainder.substring(0, nextDash) : remainder;
            Optional<Complaint> complaintOpt = complaintRepository.findByTicketId(ticketIdCandidate.toUpperCase());

            if (complaintOpt.isPresent()) {
                Complaint complaint = complaintOpt.get();
                // Check student privacy
                if (resolvedRole.equalsIgnoreCase("STUDENT")) {
                    if (resolvedEmail == null || !complaint.getStudentEmail().equalsIgnoreCase(resolvedEmail.trim())) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
                // Check department authorization
                if (!resolvedRole.equalsIgnoreCase("ADMIN") && !resolvedRole.equalsIgnoreCase("STUDENT")) {
                    Department staffDept = Department.fromString(resolvedRole);
                    if (staffDept != null && staffDept != complaint.getDepartment()) {
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                    }
                }
            }
        }

        Resource resource = fileStorageService.loadEvidenceFile(filename);
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
}
