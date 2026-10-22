package com.campusreporter.service;

import com.campusreporter.dto.ComplaintRequestDTO;
import com.campusreporter.dto.ComplaintResponseDTO;
import com.campusreporter.dto.DepartmentActionDTO;

import java.util.List;
import java.util.Map;

public interface ComplaintService {

    ComplaintResponseDTO submitComplaint(ComplaintRequestDTO request, String authUserEmail, String authUserRole);

    ComplaintResponseDTO submitComplaint(ComplaintRequestDTO request, org.springframework.web.multipart.MultipartFile[] files, String authUserEmail, String authUserRole);

    List<ComplaintResponseDTO> getMyComplaints(String authUserEmail);

    List<ComplaintResponseDTO> getDepartmentComplaints(String departmentCode, String authUserRole);

    ComplaintResponseDTO getComplaintByTicketId(String ticketId, String authUserEmail, String authUserRole);

    ComplaintResponseDTO acceptComplaint(String ticketId, DepartmentActionDTO action, String staffEmail, String staffRole);

    ComplaintResponseDTO declineComplaint(String ticketId, DepartmentActionDTO action, String staffEmail, String staffRole);

    ComplaintResponseDTO startWork(String ticketId, DepartmentActionDTO action, String staffEmail, String staffRole);

    ComplaintResponseDTO submitEvidence(String ticketId, DepartmentActionDTO action, String staffEmail, String staffRole);

    ComplaintResponseDTO completeWorkWithEvidence(String ticketId, org.springframework.web.multipart.MultipartFile[] files, String acknowledgement, String staffName, String staffEmail, String staffRole);

    ComplaintResponseDTO verifyResolution(String ticketId, DepartmentActionDTO action, String studentEmail);

    ComplaintResponseDTO disputeResolution(String ticketId, DepartmentActionDTO action, String studentEmail);

    List<ComplaintResponseDTO> getAllComplaintsAdmin();

    List<ComplaintResponseDTO> getAllComplaintsAdmin(String statusFilter);

    Map<String, Long> getStudentMetrics(String authUserEmail);

    Map<String, Long> getDepartmentMetrics(String departmentCode);

    Map<String, Long> getDepartmentMetrics(String departmentCode, String authUserRole);

    Map<String, Object> getAdminMetrics();
    int checkAndEscalateOverdueComplaints();
}
