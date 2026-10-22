package com.campusreporter.repository;

import com.campusreporter.entity.ComplaintAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ComplaintAuditLogRepository extends JpaRepository<ComplaintAuditLog, Long> {
    List<ComplaintAuditLog> findByTicketIdOrderByActionTimestampDesc(String ticketId);
    List<ComplaintAuditLog> findByComplaintIdOrderByActionTimestampDesc(Long complaintId);
}
