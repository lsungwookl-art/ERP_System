package com.erp.master.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class ItemRequest {
    private String itemCode;
    @NotBlank private String itemName;
    private Long categoryId;
    private String unit;
    private BigDecimal standardPrice;
    private BigDecimal standardCost;
    private BigDecimal safetyStock;
    private String description;
}
