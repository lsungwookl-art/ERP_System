package com.erp.inventory.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record StockBalanceDto(
        Long id,
        Long itemId,
        String itemCode,
        String itemName,
        String unit,
        Long warehouseId,
        String warehouseName,
        BigDecimal qty,
        BigDecimal avgCost,
        BigDecimal stockValue,
        BigDecimal safetyStock,
        boolean belowSafety,
        LocalDateTime lastMovedAt
) {}
