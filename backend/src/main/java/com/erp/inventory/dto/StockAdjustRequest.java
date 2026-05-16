package com.erp.inventory.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class StockAdjustRequest {
    @NotNull private Long itemId;
    @NotNull private Long warehouseId;
    @NotNull private BigDecimal newQty;
    private BigDecimal newCost;
}
