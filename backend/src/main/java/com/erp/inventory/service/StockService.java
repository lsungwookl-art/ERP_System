package com.erp.inventory.service;

import com.erp.inventory.entity.StockBalance;
import com.erp.inventory.entity.StockMovement;
import com.erp.inventory.entity.StockMovement.MovementType;
import com.erp.inventory.repository.StockBalanceRepository;
import com.erp.inventory.repository.StockMovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockBalanceRepository stockBalanceRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional
    public BigDecimal receiveStock(Long companyId, Long itemId, Long warehouseId,
                                    BigDecimal qty, BigDecimal unitPrice,
                                    String refType, Long refId, String operator) {
        StockBalance balance = getOrCreateBalance(companyId, itemId, warehouseId);
        BigDecimal beforeQty = balance.getQty();
        balance.receiveStock(qty, unitPrice);

        recordMovement(companyId, itemId, warehouseId, MovementType.IN,
                qty, unitPrice, beforeQty, balance.getQty(), refType, refId, operator);

        return balance.getAvgCost();
    }

    @Transactional
    public BigDecimal issueStock(Long companyId, Long itemId, Long warehouseId,
                                  BigDecimal qty, String refType, Long refId, String operator) {
        StockBalance balance = stockBalanceRepository
                .findForUpdate(companyId, itemId, warehouseId)
                .orElseThrow(() -> new IllegalStateException("재고 정보가 없습니다."));

        BigDecimal avgCost = balance.getAvgCost();
        BigDecimal beforeQty = balance.getQty();
        balance.issueStock(qty);

        recordMovement(companyId, itemId, warehouseId, MovementType.OUT,
                qty, avgCost, beforeQty, balance.getQty(), refType, refId, operator);

        return avgCost;
    }

    @Transactional
    public void adjustStock(Long companyId, Long itemId, Long warehouseId,
                             BigDecimal newQty, BigDecimal newCost, String operator) {
        StockBalance balance = getOrCreateBalance(companyId, itemId, warehouseId);
        BigDecimal beforeQty = balance.getQty();
        BigDecimal diff = newQty.subtract(beforeQty);

        balance.adjustStock(newQty, newCost);

        recordMovement(companyId, itemId, warehouseId, MovementType.ADJUST,
                diff, newCost, beforeQty, newQty, "ADJUST", null, operator);
    }

    private StockBalance getOrCreateBalance(Long companyId, Long itemId, Long warehouseId) {
        return stockBalanceRepository
                .findByCompanyIdAndItemIdAndWarehouseId(companyId, itemId, warehouseId)
                .orElseGet(() -> stockBalanceRepository.save(
                        StockBalance.builder()
                                .companyId(companyId)
                                .itemId(itemId)
                                .warehouseId(warehouseId)
                                .build()));
    }

    private void recordMovement(Long companyId, Long itemId, Long warehouseId,
                                  MovementType type, BigDecimal qty, BigDecimal unitCost,
                                  BigDecimal beforeQty, BigDecimal afterQty,
                                  String refType, Long refId, String createdBy) {
        stockMovementRepository.save(StockMovement.builder()
                .companyId(companyId)
                .itemId(itemId)
                .warehouseId(warehouseId)
                .movementType(type)
                .qty(qty)
                .unitCost(unitCost)
                .beforeQty(beforeQty)
                .afterQty(afterQty)
                .refType(refType)
                .refId(refId)
                .movementDate(LocalDate.now())
                .createdBy(createdBy)
                .build());
    }
}
