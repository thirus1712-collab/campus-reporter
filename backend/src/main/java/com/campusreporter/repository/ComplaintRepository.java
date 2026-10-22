package com.campusreporter.repository;

import com.campusreporter.entity.Complaint;
import com.campusreporter.model.ComplaintStatus;
import com.campusreporter.model.Department;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long> {

    Optional<Complaint> findByTicketId(String ticketId);

    List<Complaint> findByStudentEmailOrderBySubmittedAtDesc(String studentEmail);

    List<Complaint> findByDepartmentOrderBySubmittedAtDesc(Department department);

    List<Complaint> findByDepartmentAndStatusOrderBySubmittedAtDesc(Department department, ComplaintStatus status);

    Optional<Complaint> findByIdempotencyKey(String idempotencyKey);

    boolean existsByTicketId(String ticketId);

    long countByStudentEmail(String studentEmail);

    long countByStudentEmailAndStatus(String studentEmail, ComplaintStatus status);

    long countByDepartment(Department department);

    long countByDepartmentAndStatus(Department department, ComplaintStatus status);

    List<Complaint> findAllByOrderBySubmittedAtDesc();

    List<Complaint> findByEmergencyTrueOrderBySubmittedAtDesc();

    long countByStatus(ComplaintStatus status);

    long countByEmergencyTrue();

    @Query("SELECT MAX(c.id) FROM Complaint c")
    Long findMaxId();

    List<Complaint> findByStatusInAndWorkDeadlineBeforeAndEscalatedToAdminFalse(List<ComplaintStatus> statuses, java.time.LocalDateTime deadline);

    List<Complaint> findByAcceptedAtNotNullAndWorkDeadlineIsNull();

    long countByEscalatedToAdminTrue();
}
