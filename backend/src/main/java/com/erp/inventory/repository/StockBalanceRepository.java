package com.erp.inventory.repository;

import com.erp.inventory.entity.StockBalance;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.List;

public interface StockBalanceRepository extends JpaRepository<StockBalance, Long> {

    @Lock(LockModeType.OPTIMISTIC)
    Optional<StockBalance> findByCompanyIdAndItemIdAndWarehouseId(
            Long companyId, Long itemId, Long warehouseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM StockBalance s WHERE s.companyId = :companyId AND s.itemId = :itemId AND s.warehouseId = :warehouseId")
    Optional<StockBalance> findForUpdate(@Param("companyId") Long companyId,
                                          @Param("itemId") Long itemId,
                                          @Param("warehouseId") Long warehouseId);

    @Query("SELECT s FROM StockBalance s WHERE s.companyId = :companyId " +
           "AND (:itemId IS NULL OR s.itemId = :itemId) " +
           "AND (:warehouseId IS NULL OR s.warehouseId = :warehouseId)")
    Page<StockBalance> findByCompanyIdAndFilter(@Param("companyId") Long companyId,
                                                 @Param("itemId") Long itemId,
                                                 @Param("warehouseId") Long warehouseId,
                                                 Pageable pageable);

    @Query("SELECT COUNT(s) FROM StockBalance s " +
           "JOIN com.erp.master.entity.Item i ON s.itemId = i.id " +
           "WHERE s.companyId = :companyId AND i.deleted = false AND s.qty < i.safetyStock AND i.safetyStock > 0")
    long countStockAlerts(@Param("companyId") Long companyId);
}
