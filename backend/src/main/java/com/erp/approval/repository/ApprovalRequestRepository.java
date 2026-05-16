package com.erp.approval.repository;

import com.erp.approval.entity.ApprovalRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {

    @Query("SELECT r FROM ApprovalRequest r WHERE r.companyId = :companyId AND r.deleted = false " +
           "AND (:status IS NULL OR r.status = :status) " +
           "ORDER BY r.createdAt DESC")
    Page<ApprovalRequest> findByFilter(@Param("companyId") Long companyId,
                                        @Param("status") ApprovalRequest.ApprovalStatus status,
                                        Pageable pageable);

    @Query("SELECT COUNT(r) FROM ApprovalRequest r " +
           "JOIN r.lines l " +
           "WHERE r.companyId = :companyId AND r.deleted = false " +
           "AND r.status = 'PENDING' AND l.approverId = :approverId AND l.status = 'PENDING'")
    long countPendingForApprover(@Param("companyId") Long companyId,
                                  @Param("approverId") Long approverId);
}
