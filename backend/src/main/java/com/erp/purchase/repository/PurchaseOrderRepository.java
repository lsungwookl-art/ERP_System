package com.erp.purchase.repository;

import com.erp.purchase.entity.PurchaseOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {
    Optional<PurchaseOrder> findByIdAndCompanyIdAndDeletedFalse(Long id, Long companyId);
    Page<PurchaseOrder> findByCompanyIdAndDeletedFalse(Long companyId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM PurchaseOrder o WHERE o.companyId = :companyId " +
           "AND o.deleted = false AND CAST(o.status AS string) IN :statuses")
    long countByCompanyIdAndStatusIn(@Param("companyId") Long companyId,
                                      @Param("statuses") List<String> statuses);
}
