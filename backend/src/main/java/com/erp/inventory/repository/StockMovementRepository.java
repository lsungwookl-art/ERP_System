package com.erp.inventory.repository;

import com.erp.inventory.entity.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {

    @Query("SELECT m FROM StockMovement m WHERE m.companyId = :companyId " +
           "AND (:itemId IS NULL OR m.itemId = :itemId) " +
           "AND (:warehouseId IS NULL OR m.warehouseId = :warehouseId) " +
           "AND (:fromDate IS NULL OR m.movementDate >= :fromDate) " +
           "AND (:toDate IS NULL OR m.movementDate <= :toDate) " +
           "ORDER BY m.createdAt DESC")
    Page<StockMovement> findByFilter(@Param("companyId") Long companyId,
                                      @Param("itemId") Long itemId,
                                      @Param("warehouseId") Long warehouseId,
                                      @Param("fromDate") LocalDate fromDate,
                                      @Param("toDate") LocalDate toDate,
                                      Pageable pageable);
}
