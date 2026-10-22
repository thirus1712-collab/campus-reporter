package com.campusreporter.service;

import com.campusreporter.dto.ComplaintRequestDTO;
import com.campusreporter.dto.ComplaintResponseDTO;
import com.campusreporter.dto.DepartmentActionDTO;
import com.campusreporter.entity.Complaint;
import com.campusreporter.entity.ComplaintAuditLog;
import com.campusreporter.exception.AuthenticationException;
import com.campusreporter.exception.ResourceNotFoundException;
import com.campusreporter.exception.UnauthorizedException;
import com.campusreporter.exception.ValidationException;
import com.campusreporter.model.CampusLocations;
import com.campusreporter.model.ComplaintStatus;
import com.campusreporter.model.Department;
import com.campusreporter.repository.ComplaintAuditLogRepository;
import com.campusreporter.repository.ComplaintRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ComplaintServiceImpl implements ComplaintService {

    private static final Logger logger = LoggerFactory.getLogger(ComplaintServiceImpl.class);

    private final ComplaintRepository complaintRepository;
    private final ComplaintAuditLogRepository auditLogRepository;
    private final TicketIdGenerator ticketIdGenerator;
    private final FileStorageService fileStorageService;

    public ComplaintServiceImpl(ComplaintRepository complaintRepository,
                                ComplaintAuditLogRepository auditLogRepository,
                                TicketIdGenerator ticketIdGenerator,
                                FileStorageService fileStorageService) {
        this.complaintRepository = complaintRepository;
        this.auditLogRepository = auditLogRepository;
        this.ticketIdGenerator = ticketIdGenerator;
        this.fileStorageService = fileStorageService;
    }

    @Override
    public ComplaintResponseDTO submitComplaint(ComplaintRequestDTO request, String authUserEmail, String authUserRole) {
        return submitComplaint(request, null, authUserEmail, authUserRole);
    }

    @Override
    @Transactional
    public ComplaintResponseDTO submitComplaint(ComplaintRequestDTO request, org.springframework.web.multipart.MultipartFile[] files, String authUserEmail, String authUserRole) {
        if (request == null) {
            throw new ValidationException("Complaint request payload cannot be empty");
        }

        // 1. Check Idempotency Key to prevent duplicate submissions
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().trim().isEmpty()) {
            Optional<Complaint> existing = complaintRepository.findByIdempotencyKey(request.getIdempotencyKey().trim());
            if (existing.isPresent()) {
                return ComplaintResponseDTO.fromEntity(existing.get());
            }
        }

        // 2. Validate Department
        Department dept = Department.fromString(request.getDepartment());
        if (dept == null) {
            throw new ValidationException("Invalid department. Supported departments are: Electrical, Plumbing, Cleaning, IT & Technical Support");
        }

        // 3. Validate Campus Location (Block, Floor, Classroom/Room)
        if (!CampusLocations.isValidBlock(request.getCampusBlock())) {
            throw new ValidationException("Invalid campus block: " + request.getCampusBlock());
        }

        if (!CampusLocations.isValidFloor(request.getFloorLevel())) {
            throw new ValidationException("Invalid floor level: " + request.getFloorLevel());
        }

        if (!CampusLocations.isValidLocation(request.getFloorLevel(), request.getSpecificLocation())) {
            throw new ValidationException("Invalid specific location '" + request.getSpecificLocation() + "' for floor '" + request.getFloorLevel() + "'");
        }

        // 4. Validate Title and Description Length
        String title = request.getIssueTitle() != null ? request.getIssueTitle().trim() : "";
        if (title.length() < 5) {
            throw new ValidationException("Issue summary title must be at least 5 characters long");
        }

        String desc = request.getDescription() != null ? request.getDescription().trim() : "";
        if (desc.length() < 15) {
            throw new ValidationException("Problem description must be at least 15 characters long");
        }

        // 5. Validate Phone Number (must contain at least 10 digits)
        String phone = request.getStudentPhone() != null ? request.getStudentPhone().trim() : "";
        String phoneDigits = phone.replaceAll("\\D", "");
        if (phoneDigits.length() < 10) {
            throw new ValidationException("Contact phone number must have at least 10 digits");
        }

        // 6. Resolve Student Identity
        String studentEmail = (authUserEmail != null && !authUserEmail.trim().isEmpty())
            ? authUserEmail.trim()
            : (request.getStudentEmail() != null && !request.getStudentEmail().trim().isEmpty()
                ? request.getStudentEmail().trim()
                : "student_2024@campus.edu");

        String studentName = request.getStudentName() != null && !request.getStudentName().trim().isEmpty()
            ? request.getStudentName().trim()
            : studentEmail.split("@")[0];

        // 7. Generate Ticket ID and backend submission timestamp
        String ticketId = ticketIdGenerator.generateTicketId();
        LocalDateTime now = LocalDateTime.now();

        // 8. Construct Entity
        Complaint complaint = new Complaint();
        complaint.setTicketId(ticketId);
        complaint.setStudentEmail(studentEmail);
        complaint.setStudentName(studentName);
        complaint.setDepartment(dept);
        complaint.setCampusBlock(request.getCampusBlock().trim());
        complaint.setFloorLevel(request.getFloorLevel().trim());
        complaint.setSpecificLocation(request.getSpecificLocation().trim());
        complaint.setIssueTitle(title);
        complaint.setDescription(desc);
        complaint.setStudentPhone(phone);
        complaint.setEmergency(request.isEmergency());
        complaint.setStatus(ComplaintStatus.SUBMITTED);
        complaint.setSubmittedAt(now);
        complaint.setUpdatedAt(now);
        complaint.setIdempotencyKey(request.getIdempotencyKey());

        // Handle uploaded images (files or URLs)
        if (files != null && files.length > 0) {
            List<String> storedUrls = fileStorageService.storeComplaintImages(ticketId, Arrays.asList(files));
            complaint.setImageUrls(storedUrls);
        } else if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            if (request.getImageUrls().size() > 5) {
                throw new ValidationException("A maximum of 5 complaint images are permitted.");
            }
            complaint.setImageUrls(new ArrayList<>(request.getImageUrls()));
        }

        // 9. Persist to PostgreSQL
        Complaint saved = complaintRepository.save(complaint);

        // 10. Record Audit Log
        ComplaintAuditLog log = new ComplaintAuditLog(
            saved.getId(),
            saved.getTicketId(),
            "COMPLAINT_SUBMITTED",
            null,
            ComplaintStatus.SUBMITTED.name(),
            studentEmail,
            "Complaint created with initial status Submitted" +
                (saved.getImageUrls() != null && !saved.getImageUrls().isEmpty() ? " with " + saved.getImageUrls().size() + " photos attached" : "")
        );
        auditLogRepository.save(log);

        return ComplaintResponseDTO.fromEntity(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponseDTO> getMyComplaints(String authUserEmail) {
        String email = (authUserEmail != null && !authUserEmail.trim().isEmpty())
            ? authUserEmail.trim()
            : "student_2024@campus.edu";

        List<Complaint> complaints = complaintRepository.findByStudentEmailOrderBySubmittedAtDesc(email);
        return complaints.stream()
            .map(ComplaintResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ComplaintResponseDTO> getDepartmentComplaints(String departmentCode, String authUserRole) {
        Department dept = Department.fromString(departmentCode);
        if (dept == null) {
            throw new ValidationException("Invalid department code: " + departmentCode);
        }

        // Authorization check: students or unauthorized callers cannot access department queues
        if (authUserRole == null || authUserRole.trim().isEmpty() || authUserRole.equalsIgnoreCase("STUDENT")) {
            throw new UnauthorizedException("Access denied: You are not authorized to view the " + dept.getDisplayName() + " queue.");
        }

        // Authorization check: department users cannot access other department queues
        if (!authUserRole.equalsIgnoreCase("ADMIN")) {
            Department authorizedDept = Department.fromString(authUserRole);
            if (authorizedDept == null || authorizedDept != dept) {
                throw new UnauthorizedException("Department staff from " + (authorizedDept != null ? authorizedDept.getDisplayName() : authUserRole) +
                    " are not authorized to view the " + dept.getDisplayName() + " queue.");
            }
        }

        List<Complaint> complaints = complaintRepository.findByDepartmentOrderBySubmittedAtDesc(dept);
        return complaints.stream()
            .map(ComplaintResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ComplaintResponseDTO getComplaintByTicketId(String ticketId, String authUserEmail, String authUserRole) {
        if (ticketId == null || ticketId.trim().isEmpty()) {
            throw new ValidationException("Ticket ID cannot be empty");
        }

        Complaint complaint = complaintRepository.findByTicketId(ticketId.trim())
            .orElseThrow(() -> new ResourceNotFoundException("Complaint with Ticket ID '" + ticketId + "' not found"));

        if (authUserRole == null || authUserRole.trim().isEmpty()) {
            throw new UnauthorizedException("Authentication required to view complaint details.");
        }

        // Privacy check: Students can only view their own ticket
        if (authUserRole.equalsIgnoreCase("STUDENT")) {
            if (authUserEmail == null || !complaint.getStudentEmail().equalsIgnoreCase(authUserEmail.trim())) {
                throw new UnauthorizedException("You are not authorized to view tickets filed by another student.");
            }
        }

        // Authorization check: Department staff can only view tickets assigned to their department
        if (!authUserRole.equalsIgnoreCase("ADMIN") && !authUserRole.equalsIgnoreCase("STUDENT")) {
            Department authorizedDept = Department.fromString(authUserRole);
            if (authorizedDept != null && authorizedDept != complaint.getDepartment()) {
                throw new UnauthorizedException("Department staff from " + authorizedDept.getDisplayName() +
                    " are not authorized to view tickets assigned to " + complaint.getDepartment().getDisplayName() + ".");
            }
        }

        evaluateSingleComplaintEscalation(complaint);
        return ComplaintResponseDTO.fromEntity(complaint);
    }

    @Override
    public ComplaintResponseDTO acceptComplaint(String ticketId, DepartmentActionDTO action, String staffEmail, String staffRole) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint with Ticket ID '" + ticketId + "' not found"));

        // Students cannot perform department actions
        if (staffRole != null && staffRole.equalsIgnoreCase("STUDENT")) {
            throw new UnauthorizedException("Students are not authorized to perform department maintenance actions.");
        }

        // Verify department authorization
        if (staffRole != null && !staffRole.equalsIgnoreCase("ADMIN")) {
            Department authorizedDept = Department.fromString(staffRole);
            if (authorizedDept != null && authorizedDept != complaint.getDepartment()) {
                throw new UnauthorizedException("Staff from " + authorizedDept.getDisplayName() +
                    " cannot accept tickets assigned to " + complaint.getDepartment().getDisplayName());
            }
        }

        // Enforce state transition rules
        if (complaint.getStatus() == ComplaintStatus.UNDER_REVIEW) {
            if (complaint.getWorkDeadline() == null && complaint.getAcceptedAt() != null) {
                complaint.setWorkDeadline(complaint.getAcceptedAt().plusDays(7));
                complaintRepository.save(complaint);
            }
            return ComplaintResponseDTO.fromEntity(complaint); // Idempotent
        }
        if (complaint.getStatus() == ComplaintStatus.DECLINED) {
            throw new ValidationException("Cannot accept a complaint that has already been declined.");
        }
        if (complaint.getStatus() == ComplaintStatus.CLOSED || complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new ValidationException("Cannot accept a complaint that is already resolved or closed.");
        }
        if (complaint.getStatus() != ComplaintStatus.SUBMITTED) {
            throw new ValidationException("Complaint cannot be accepted from status: " + complaint.getStatus().getDisplayName());
        }

        String staffName = (action != null && action.getStaffName() != null && !action.getStaffName().trim().isEmpty())
            ? action.getStaffName().trim()
            : (staffEmail != null ? staffEmail : "Department Staff");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = now.plusDays(7);
        String previousStatus = complaint.getStatus().name();
        complaint.setStatus(ComplaintStatus.UNDER_REVIEW);
        complaint.setAcceptedBy(staffName);
        complaint.setAcceptedAt(now);
        complaint.setWorkDeadline(deadline);
        complaint.setEscalatedToAdmin(false);
        complaint.setUpdatedAt(now);

        Complaint updated = complaintRepository.save(complaint);

        // Record Audit Event
        ComplaintAuditLog log = new ComplaintAuditLog(
            updated.getId(),
            updated.getTicketId(),
            "COMPLAINT_ACCEPTED",
            previousStatus,
            ComplaintStatus.UNDER_REVIEW.name(),
            staffName,
            (action != null && action.getNotes() != null ? action.getNotes() : "Ticket accepted by department technician") +
                "; 7-day SLA completion deadline set to " + deadline
        );
        auditLogRepository.save(log);

        return ComplaintResponseDTO.fromEntity(updated);
    }

    @Override
    public ComplaintResponseDTO declineComplaint(String ticketId, DepartmentActionDTO action, String staffEmail, String staffRole) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint with Ticket ID '" + ticketId + "' not found"));

        if (staffRole != null && staffRole.equalsIgnoreCase("STUDENT")) {
            throw new UnauthorizedException("Students are not authorized to perform department maintenance actions.");
        }

        // Verify department authorization
        if (staffRole != null && !staffRole.equalsIgnoreCase("ADMIN")) {
            Department authorizedDept = Department.fromString(staffRole);
            if (authorizedDept != null && authorizedDept != complaint.getDepartment()) {
                throw new UnauthorizedException("Staff from " + authorizedDept.getDisplayName() +
                    " cannot decline tickets assigned to " + complaint.getDepartment().getDisplayName());
            }
        }

        // Enforce state transition rules
        if (complaint.getStatus() == ComplaintStatus.DECLINED) {
            return ComplaintResponseDTO.fromEntity(complaint); // Idempotent
        }
        if (complaint.getStatus() == ComplaintStatus.IN_PROGRESS || complaint.getStatus() == ComplaintStatus.AWAITING_VERIFICATION) {
            throw new ValidationException("Cannot decline a complaint that is already in progress or completed.");
        }
        if (complaint.getStatus() == ComplaintStatus.CLOSED || complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new ValidationException("Cannot decline a complaint that is already resolved or closed.");
        }
        if (complaint.getStatus() != ComplaintStatus.SUBMITTED && complaint.getStatus() != ComplaintStatus.UNDER_REVIEW) {
            throw new ValidationException("Complaint cannot be declined from status: " + complaint.getStatus().getDisplayName());
        }

        String declineReason = (action != null && action.getReason() != null && !action.getReason().trim().isEmpty())
            ? action.getReason().trim()
            : (action != null && action.getNotes() != null ? action.getNotes().trim() : null);

        if (declineReason == null || declineReason.trim().length() < 3) {
            throw new ValidationException("A clear reason is mandatory when declining a complaint (minimum 3 characters).");
        }

        String staffName = (action != null && action.getStaffName() != null && !action.getStaffName().trim().isEmpty())
            ? action.getStaffName().trim()
            : (staffEmail != null ? staffEmail : "Department Staff");

        String previousStatus = complaint.getStatus().name();
        complaint.setStatus(ComplaintStatus.DECLINED);
        complaint.setDeclinedBy(staffName);
        complaint.setDeclinedReason(declineReason);
        complaint.setDeclinedAt(LocalDateTime.now());
        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint updated = complaintRepository.save(complaint);

        // Record Audit Event
        ComplaintAuditLog log = new ComplaintAuditLog(
            updated.getId(),
            updated.getTicketId(),
            "COMPLAINT_DECLINED",
            previousStatus,
            ComplaintStatus.DECLINED.name(),
            staffName,
            "Declined reason: " + declineReason
        );
        auditLogRepository.save(log);

        return ComplaintResponseDTO.fromEntity(updated);
    }

    @Override
    public ComplaintResponseDTO startWork(String ticketId, DepartmentActionDTO action, String staffEmail, String staffRole) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint with Ticket ID '" + ticketId + "' not found"));

        if (staffRole != null && staffRole.equalsIgnoreCase("STUDENT")) {
            throw new UnauthorizedException("Students are not authorized to perform department maintenance actions.");
        }

        if (staffRole != null && !staffRole.equalsIgnoreCase("ADMIN")) {
            Department authorizedDept = Department.fromString(staffRole);
            if (authorizedDept != null && authorizedDept != complaint.getDepartment()) {
                throw new UnauthorizedException("Staff from " + authorizedDept.getDisplayName() +
                    " cannot update tickets assigned to " + complaint.getDepartment().getDisplayName());
            }
        }

        // Enforce state transition rules
        if (complaint.getStatus() == ComplaintStatus.IN_PROGRESS) {
            return ComplaintResponseDTO.fromEntity(complaint); // Idempotent
        }
        if (complaint.getStatus() == ComplaintStatus.SUBMITTED) {
            throw new ValidationException("Complaint must be accepted and under review before starting repair work.");
        }
        if (complaint.getStatus() == ComplaintStatus.DECLINED) {
            throw new ValidationException("Cannot start repair work on a declined complaint.");
        }
        if (complaint.getStatus() == ComplaintStatus.CLOSED || complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new ValidationException("Cannot start repair work on a complaint that is already resolved or closed.");
        }
        if (complaint.getStatus() != ComplaintStatus.UNDER_REVIEW) {
            throw new ValidationException("Complaint cannot be started from status: " + complaint.getStatus().getDisplayName());
        }

        String staffName = (action != null && action.getStaffName() != null && !action.getStaffName().trim().isEmpty())
            ? action.getStaffName().trim()
            : (staffEmail != null ? staffEmail : "Department Technician");

        String previousStatus = complaint.getStatus().name();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setInProgressBy(staffName);
        complaint.setInProgressAt(LocalDateTime.now());
        if (complaint.getWorkDeadline() == null && complaint.getAcceptedAt() != null) {
            complaint.setWorkDeadline(complaint.getAcceptedAt().plusDays(7));
        }
        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint updated = complaintRepository.save(complaint);

        ComplaintAuditLog log = new ComplaintAuditLog(
            updated.getId(),
            updated.getTicketId(),
            "WORK_STARTED",
            previousStatus,
            ComplaintStatus.IN_PROGRESS.name(),
            staffName,
            action != null && action.getNotes() != null ? action.getNotes() : "Technician began maintenance repair"
        );
        auditLogRepository.save(log);

        return ComplaintResponseDTO.fromEntity(updated);
    }

    @Override
    public ComplaintResponseDTO completeWorkWithEvidence(String ticketId, MultipartFile[] files, String acknowledgement, String staffName, String staffEmail, String staffRole) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint with Ticket ID '" + ticketId + "' not found"));

        if (staffEmail == null || staffRole == null || staffRole.trim().isEmpty()) {
            throw new AuthenticationException("Authentication required. Please sign in to submit repair completion evidence.");
        }

        if (staffRole.equalsIgnoreCase("STUDENT")) {
            throw new UnauthorizedException("Students are not authorized to perform department maintenance actions.");
        }

        if (!staffRole.equalsIgnoreCase("ADMIN")) {
            Department authorizedDept = Department.fromString(staffRole);
            if (authorizedDept == null) {
                throw new UnauthorizedException("Invalid staff department role: " + staffRole);
            }
            if (authorizedDept != complaint.getDepartment()) {
                throw new UnauthorizedException("Staff from " + authorizedDept.getDisplayName() +
                    " cannot update tickets assigned to " + complaint.getDepartment().getDisplayName());
            }
        }

        // Enforce state transition rules
        if (complaint.getStatus() == ComplaintStatus.AWAITING_VERIFICATION) {
            return ComplaintResponseDTO.fromEntity(complaint); // Idempotent
        }
        if (complaint.getStatus() == ComplaintStatus.SUBMITTED) {
            throw new ValidationException("Complaint must be accepted and repair started before marking completed.");
        }
        if (complaint.getStatus() == ComplaintStatus.UNDER_REVIEW) {
            throw new ValidationException("Repair work must be started before completing the complaint.");
        }
        if (complaint.getStatus() == ComplaintStatus.DECLINED) {
            throw new ValidationException("Cannot complete a declined complaint.");
        }
        if (complaint.getStatus() == ComplaintStatus.CLOSED || complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new ValidationException("Cannot complete a complaint that is already resolved or closed.");
        }
        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            throw new ValidationException("Complaint cannot be completed from status: " + complaint.getStatus().getDisplayName());
        }

        List<MultipartFile> fileList = files != null ? Arrays.asList(files) : Collections.emptyList();
        List<String> storedUrls = fileStorageService.storeEvidenceImages(ticketId, fileList);

        String resolvedStaffName = (staffName != null && !staffName.trim().isEmpty())
            ? staffName.trim()
            : (staffEmail != null ? staffEmail : "Department Technician");

        String previousStatus = complaint.getStatus().name();
        complaint.setStatus(ComplaintStatus.AWAITING_VERIFICATION);
        complaint.setVerificationRequestedAt(LocalDateTime.now());
        complaint.setRepairEvidenceImages(storedUrls);
        complaint.setRepairNotes(acknowledgement != null && !acknowledgement.trim().isEmpty() ? acknowledgement.trim() : null);
        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint updated = complaintRepository.save(complaint);

        ComplaintAuditLog log = new ComplaintAuditLog(
            updated.getId(),
            updated.getTicketId(),
            "REPAIR_COMPLETED_WITH_EVIDENCE",
            previousStatus,
            ComplaintStatus.AWAITING_VERIFICATION.name(),
            resolvedStaffName,
            "Completed repair with " + storedUrls.size() + " proof photos" +
                (acknowledgement != null && !acknowledgement.trim().isEmpty() ? "; Note: " + acknowledgement.trim() : "")
        );
        auditLogRepository.save(log);

        return ComplaintResponseDTO.fromEntity(updated);
    }

    @Override
    public ComplaintResponseDTO submitEvidence(String ticketId, DepartmentActionDTO action, String staffEmail, String staffRole) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint with Ticket ID '" + ticketId + "' not found"));

        if (staffRole != null && staffRole.equalsIgnoreCase("STUDENT")) {
            throw new UnauthorizedException("Students are not authorized to perform department maintenance actions.");
        }

        if (staffRole != null && !staffRole.equalsIgnoreCase("ADMIN")) {
            Department authorizedDept = Department.fromString(staffRole);
            if (authorizedDept != null && authorizedDept != complaint.getDepartment()) {
                throw new UnauthorizedException("Staff from " + authorizedDept.getDisplayName() +
                    " cannot update tickets assigned to " + complaint.getDepartment().getDisplayName());
            }
        }

        if (complaint.getStatus() == ComplaintStatus.AWAITING_VERIFICATION) {
            return ComplaintResponseDTO.fromEntity(complaint);
        }
        if (complaint.getStatus() == ComplaintStatus.SUBMITTED) {
            throw new ValidationException("Complaint must be accepted and repair started before marking completed.");
        }
        if (complaint.getStatus() == ComplaintStatus.UNDER_REVIEW) {
            throw new ValidationException("Repair work must be started before completing the complaint.");
        }
        if (complaint.getStatus() == ComplaintStatus.DECLINED) {
            throw new ValidationException("Cannot complete a declined complaint.");
        }
        if (complaint.getStatus() == ComplaintStatus.CLOSED || complaint.getStatus() == ComplaintStatus.RESOLVED) {
            throw new ValidationException("Cannot complete a complaint that is already resolved or closed.");
        }
        if (complaint.getStatus() != ComplaintStatus.IN_PROGRESS) {
            throw new ValidationException("Complaint cannot be completed from status: " + complaint.getStatus().getDisplayName());
        }

        String staffName = (action != null && action.getStaffName() != null && !action.getStaffName().trim().isEmpty())
            ? action.getStaffName().trim()
            : (staffEmail != null ? staffEmail : "Department Technician");

        String previousStatus = complaint.getStatus().name();
        complaint.setStatus(ComplaintStatus.AWAITING_VERIFICATION);
        complaint.setVerificationRequestedAt(LocalDateTime.now());
        if (action != null) {
            if (action.getEvidenceImages() != null && !action.getEvidenceImages().isEmpty()) {
                if (action.getEvidenceImages().size() > 5) {
                    throw new ValidationException("A maximum of 5 repair evidence images are permitted.");
                }
                complaint.setRepairEvidenceImages(action.getEvidenceImages());
            } else if (action.getEvidenceUrl() != null && !action.getEvidenceUrl().trim().isEmpty()) {
                complaint.setRepairEvidenceUrl(action.getEvidenceUrl().trim());
            } else {
                throw new ValidationException("At least one repair evidence image is required (minimum 1, maximum 5).");
            }

            String note = action.getAcknowledgement() != null ? action.getAcknowledgement() :
                          (action.getEvidenceNotes() != null ? action.getEvidenceNotes() : action.getNotes());
            complaint.setRepairNotes(note != null && !note.trim().isEmpty() ? note.trim() : null);
        } else {
            throw new ValidationException("At least one repair evidence image is required.");
        }
        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint updated = complaintRepository.save(complaint);

        ComplaintAuditLog log = new ComplaintAuditLog(
            updated.getId(),
            updated.getTicketId(),
            "REPAIR_EVIDENCE_SUBMITTED",
            previousStatus,
            ComplaintStatus.AWAITING_VERIFICATION.name(),
            staffName,
            "Repair completed; photo evidence submitted for student verification"
        );
        auditLogRepository.save(log);

        return ComplaintResponseDTO.fromEntity(updated);
    }

    @Override
    public ComplaintResponseDTO verifyResolution(String ticketId, DepartmentActionDTO action, String studentEmail) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint with Ticket ID '" + ticketId + "' not found"));

        if (studentEmail != null && !studentEmail.trim().isEmpty()) {
            if (!complaint.getStudentEmail().equalsIgnoreCase(studentEmail.trim())) {
                throw new UnauthorizedException("Only the student who reported this issue can verify its resolution.");
            }
        }

        String verifierName = (action != null && action.getStaffName() != null && !action.getStaffName().trim().isEmpty())
            ? action.getStaffName().trim()
            : (studentEmail != null ? studentEmail : complaint.getStudentName());

        String previousStatus = complaint.getStatus().name();
        complaint.setStatus(ComplaintStatus.CLOSED);
        complaint.setResolvedBy(verifierName);
        complaint.setResolvedAt(LocalDateTime.now());
        if (action != null && action.getFeedback() != null) {
            complaint.setStudentFeedback(action.getFeedback());
        }
        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint updated = complaintRepository.save(complaint);

        ComplaintAuditLog log = new ComplaintAuditLog(
            updated.getId(),
            updated.getTicketId(),
            "TICKET_VERIFIED_AND_CLOSED",
            previousStatus,
            ComplaintStatus.CLOSED.name(),
            verifierName,
            action != null && action.getNotes() != null ? action.getNotes() : "Student inspected location and confirmed satisfactory repair"
        );
        auditLogRepository.save(log);

        return ComplaintResponseDTO.fromEntity(updated);
    }

    @Override
    public ComplaintResponseDTO disputeResolution(String ticketId, DepartmentActionDTO action, String studentEmail) {
        Complaint complaint = complaintRepository.findByTicketId(ticketId)
            .orElseThrow(() -> new ResourceNotFoundException("Complaint with Ticket ID '" + ticketId + "' not found"));

        if (studentEmail != null && !studentEmail.trim().isEmpty()) {
            if (!complaint.getStudentEmail().equalsIgnoreCase(studentEmail.trim())) {
                throw new UnauthorizedException("Only the student who reported this issue can dispute its resolution.");
            }
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reworkDeadline = now.plusDays(7);
        String previousStatus = complaint.getStatus().name();
        complaint.setStatus(ComplaintStatus.IN_PROGRESS);
        complaint.setReworkStartedAt(now);
        complaint.setWorkDeadline(reworkDeadline);
        complaint.setVerificationRequestedAt(null); // Cleared so staff completion countdown runs for rework
        complaint.setEscalatedToAdmin(false); // Reset escalation for the new rework period
        complaint.setEscalatedAt(null);
        complaint.setEscalationReason(null);
        if (action != null && action.getReason() != null) {
            complaint.setStudentFeedback("Dispute: " + action.getReason());
        }
        complaint.setUpdatedAt(now);

        Complaint updated = complaintRepository.save(complaint);

        String disputeReason = (action != null && action.getReason() != null)
            ? action.getReason()
            : "Student requested rework after inspecting repair evidence";

        ComplaintAuditLog log = new ComplaintAuditLog(
            updated.getId(),
            updated.getTicketId(),
            "RESOLUTION_DISPUTED",
            previousStatus,
            ComplaintStatus.IN_PROGRESS.name(),
            studentEmail,
            disputeReason + "; new 7-day rework deadline set to " + reworkDeadline
        );
        auditLogRepository.save(log);

        return ComplaintResponseDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public List<ComplaintResponseDTO> getAllComplaintsAdmin() {
        return getAllComplaintsAdmin(null);
    }

    @Override
    @Transactional
    public List<ComplaintResponseDTO> getAllComplaintsAdmin(String statusFilter) {
        checkAndEscalateOverdueComplaints();
        List<Complaint> all = complaintRepository.findAllByOrderBySubmittedAtDesc();
        if (statusFilter == null || statusFilter.trim().isEmpty() || statusFilter.equalsIgnoreCase("ALL")) {
            return all.stream()
                .map(ComplaintResponseDTO::fromEntity)
                .collect(Collectors.toList());
        }

        String filterClean = statusFilter.trim().toUpperCase().replace(" ", "_");
        return all.stream()
            .filter(c -> {
                if (filterClean.equals("RESOLVED_CLOSED") || filterClean.equals("RESOLVED")) {
                    return c.getStatus() == ComplaintStatus.RESOLVED || c.getStatus() == ComplaintStatus.CLOSED;
                }
                if (filterClean.equals("STUDENT_VERIFICATION") || filterClean.equals("VERIFICATION") || filterClean.equals("AWAITING_VERIFICATION")) {
                    return c.getStatus() == ComplaintStatus.AWAITING_VERIFICATION;
                }
                ComplaintStatus target = ComplaintStatus.fromString(filterClean);
                return c.getStatus() == target;
            })
            .map(ComplaintResponseDTO::fromEntity)
            .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getStudentMetrics(String authUserEmail) {
        String email = (authUserEmail != null && !authUserEmail.trim().isEmpty())
            ? authUserEmail.trim()
            : "student_2024@campus.edu";

        Map<String, Long> metrics = new HashMap<>();
        metrics.put("total", complaintRepository.countByStudentEmail(email));
        metrics.put("submitted", complaintRepository.countByStudentEmailAndStatus(email, ComplaintStatus.SUBMITTED));
        metrics.put("underReview", complaintRepository.countByStudentEmailAndStatus(email, ComplaintStatus.UNDER_REVIEW));
        metrics.put("inProgress", complaintRepository.countByStudentEmailAndStatus(email, ComplaintStatus.IN_PROGRESS));
        metrics.put("studentVerification", complaintRepository.countByStudentEmailAndStatus(email, ComplaintStatus.AWAITING_VERIFICATION));
        metrics.put("awaitingVerification", complaintRepository.countByStudentEmailAndStatus(email, ComplaintStatus.AWAITING_VERIFICATION));
        metrics.put("resolved", complaintRepository.countByStudentEmailAndStatus(email, ComplaintStatus.RESOLVED) +
                                complaintRepository.countByStudentEmailAndStatus(email, ComplaintStatus.CLOSED));
        metrics.put("closed", complaintRepository.countByStudentEmailAndStatus(email, ComplaintStatus.CLOSED));
        metrics.put("declined", complaintRepository.countByStudentEmailAndStatus(email, ComplaintStatus.DECLINED));
        return metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDepartmentMetrics(String departmentCode) {
        return getDepartmentMetrics(departmentCode, "ADMIN");
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getDepartmentMetrics(String departmentCode, String authUserRole) {
        Department dept = Department.fromString(departmentCode);
        if (dept == null) {
            throw new ValidationException("Invalid department code: " + departmentCode);
        }

        // Authorization check: students or unauthorized callers cannot access department metrics
        if (authUserRole == null || authUserRole.trim().isEmpty() || authUserRole.equalsIgnoreCase("STUDENT")) {
            throw new UnauthorizedException("Access denied: You are not authorized to view " + dept.getDisplayName() + " metrics.");
        }

        // Authorization check: department users cannot access other department metrics
        if (!authUserRole.equalsIgnoreCase("ADMIN")) {
            Department authorizedDept = Department.fromString(authUserRole);
            if (authorizedDept == null || authorizedDept != dept) {
                throw new UnauthorizedException("Department staff from " + (authorizedDept != null ? authorizedDept.getDisplayName() : authUserRole) +
                    " are not authorized to view " + dept.getDisplayName() + " metrics.");
            }
        }

        Map<String, Long> metrics = new HashMap<>();
        metrics.put("total", complaintRepository.countByDepartment(dept));
        metrics.put("submitted", complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.SUBMITTED));
        metrics.put("unreviewed", complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.SUBMITTED));
        metrics.put("underReview", complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.UNDER_REVIEW));
        metrics.put("inProgress", complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.IN_PROGRESS));
        metrics.put("awaitingVerification", complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.AWAITING_VERIFICATION));
        metrics.put("studentVerification", complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.AWAITING_VERIFICATION));
        metrics.put("resolved", complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.RESOLVED) +
                                complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.CLOSED));
        metrics.put("closed", complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.CLOSED));
        metrics.put("declined", complaintRepository.countByDepartmentAndStatus(dept, ComplaintStatus.DECLINED));
        return metrics;
    }

    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getAdminMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        long total = complaintRepository.count();
        long submitted = complaintRepository.countByStatus(ComplaintStatus.SUBMITTED);
        long underReview = complaintRepository.countByStatus(ComplaintStatus.UNDER_REVIEW);
        long inProgress = complaintRepository.countByStatus(ComplaintStatus.IN_PROGRESS);
        long studentVerification = complaintRepository.countByStatus(ComplaintStatus.AWAITING_VERIFICATION);
        long resolved = complaintRepository.countByStatus(ComplaintStatus.RESOLVED);
        long closed = complaintRepository.countByStatus(ComplaintStatus.CLOSED);
        long resolvedClosed = resolved + closed;
        long declined = complaintRepository.countByStatus(ComplaintStatus.DECLINED);

        metrics.put("total", total);
        metrics.put("submitted", submitted);
        metrics.put("underReview", underReview);
        metrics.put("inProgress", inProgress);
        metrics.put("studentVerification", studentVerification);
        metrics.put("awaitingVerification", studentVerification);
        metrics.put("resolvedClosed", resolvedClosed);
        metrics.put("resolved", resolvedClosed);
        metrics.put("closed", closed);
        metrics.put("declined", declined);

        Map<String, Long> deptCounts = new HashMap<>();
        for (Department d : Department.values()) {
            deptCounts.put(d.name(), complaintRepository.countByDepartment(d));
        }
        metrics.put("departmentBreakdown", deptCounts);

        return metrics;
    }

    @Override
    @Transactional
    public int checkAndEscalateOverdueComplaints() {
        LocalDateTime now = LocalDateTime.now();
        List<ComplaintStatus> activeStatuses = List.of(ComplaintStatus.UNDER_REVIEW, ComplaintStatus.IN_PROGRESS);
        List<Complaint> overdue = complaintRepository.findByStatusInAndWorkDeadlineBeforeAndEscalatedToAdminFalse(activeStatuses, now);

        int count = 0;
        for (Complaint complaint : overdue) {
            if (complaint.getVerificationRequestedAt() == null && !complaint.isEscalatedToAdmin()) {
                complaint.setEscalatedToAdmin(true);
                complaint.setEscalatedAt(now);
                complaint.setEscalationReason("7-day staff completion deadline exceeded without submission for student verification (Deadline: " + complaint.getWorkDeadline() + ")");
                complaint.setUpdatedAt(now);
                complaintRepository.save(complaint);

                ComplaintAuditLog log = new ComplaintAuditLog(
                    complaint.getId(),
                    complaint.getTicketId(),
                    "ESCALATED_TO_ADMIN",
                    complaint.getStatus().name(),
                    complaint.getStatus().name(),
                    "System SLA Monitor",
                    "Ticket automatically escalated to Admin: 7-day staff completion deadline passed on " + complaint.getWorkDeadline()
                );
                auditLogRepository.save(log);
                count++;
            }
        }
        return count;
    }

    @Scheduled(fixedRate = 60000)
    public void scheduledEscalationCheck() {
        try {
            int escalated = checkAndEscalateOverdueComplaints();
            if (escalated > 0) {
                logger.info("SLA Escalation Check: Escalated {} overdue tickets to Admin.", escalated);
            }
        } catch (Exception e) {
            logger.error("Error during scheduled escalation check: {}", e.getMessage());
        }
    }

    private void evaluateSingleComplaintEscalation(Complaint complaint) {
        if (complaint == null || complaint.getWorkDeadline() == null) return;
        LocalDateTime now = LocalDateTime.now();
        if (now.isAfter(complaint.getWorkDeadline())
            && complaint.getVerificationRequestedAt() == null
            && !complaint.isEscalatedToAdmin()
            && (complaint.getStatus() == ComplaintStatus.UNDER_REVIEW || complaint.getStatus() == ComplaintStatus.IN_PROGRESS)) {

            complaint.setEscalatedToAdmin(true);
            complaint.setEscalatedAt(now);
            complaint.setEscalationReason("7-day staff completion deadline exceeded without submission for student verification (Deadline: " + complaint.getWorkDeadline() + ")");
            complaint.setUpdatedAt(now);
            complaintRepository.save(complaint);

            ComplaintAuditLog log = new ComplaintAuditLog(
                complaint.getId(),
                complaint.getTicketId(),
                "ESCALATED_TO_ADMIN",
                complaint.getStatus().name(),
                complaint.getStatus().name(),
                "System SLA Monitor",
                "Ticket automatically escalated to Admin: 7-day staff completion deadline passed on " + complaint.getWorkDeadline()
            );
            auditLogRepository.save(log);
        }
    }
}
