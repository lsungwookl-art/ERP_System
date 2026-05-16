package com.erp.sales.repository;

import com.erp.sales.entity.SalesShipment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SalesShipmentRepository extends JpaRepository<SalesShipment, Long> {
    Optional<SalesShipment> findByIdAndCompanyIdAndDeletedFalse(Long id, Long companyId);
    Page<SalesShipment> findByCompanyIdAndDeletedFalse(Long companyId, Pageable pageable);
}
