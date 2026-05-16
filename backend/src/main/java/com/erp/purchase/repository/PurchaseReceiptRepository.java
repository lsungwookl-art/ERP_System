package com.erp.purchase.repository;

import com.erp.purchase.entity.PurchaseReceipt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PurchaseReceiptRepository extends JpaRepository<PurchaseReceipt, Long> {
    Optional<PurchaseReceipt> findByIdAndCompanyIdAndDeletedFalse(Long id, Long companyId);
    Page<PurchaseReceipt> findByCompanyIdAndDeletedFalse(Long companyId, Pageable pageable);
}
