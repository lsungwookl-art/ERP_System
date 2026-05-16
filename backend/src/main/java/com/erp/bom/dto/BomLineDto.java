package com.erp.bom.dto;

import java.math.BigDecimal;

public record BomLineDto(
        Long id,
        Long childItemId,
        String childItemCode,
        String childItemName,
        BigDecimal qty,
        String unit,
        String remark
) {}
