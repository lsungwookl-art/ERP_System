package com.erp.sales.repository;

import com.erp.sales.entity.SalesOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long> {
    Optional<SalesOrder> findByIdAndCompanyIdAndDeletedFalse(Long id, Long companyId);
    Page<SalesOrder> findByCompanyIdAndDeletedFalse(Long companyId, Pageable pageable);

    @Query("SELECT COUNT(o) FROM SalesOrder o WHERE o.companyId = :companyId " +
           "AND o.deleted = false AND CAST(o.status AS string) IN :statuses")
    long countByCompanyIdAndStatusIn(@Param("companyId") Long companyId,
                                      @Param("statuses") List<String> statuses);
}
