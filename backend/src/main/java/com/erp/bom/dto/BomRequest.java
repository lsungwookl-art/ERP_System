package com.erp.bom.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
public class BomRequest {
    @NotNull private Long parentItemId;
    @NotNull private Long childItemId;
    @NotNull @DecimalMin("0.0001") private BigDecimal qty;
    private String unit;
    private String remark;
}
