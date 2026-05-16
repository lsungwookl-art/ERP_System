package com.erp.sales.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class SalesShipmentRequest {
    @NotNull private Long orderId;
    @NotNull private Long warehouseId;
    @NotNull private LocalDate shipmentDate;
    private String remark;

    @NotEmpty @Valid
    private List<ShipmentItemDto> items;

    @Getter
    public static class ShipmentItemDto {
        @NotNull private Long orderItemId;
        @NotNull private Long itemId;
        @NotNull private BigDecimal qty;
        @NotNull private BigDecimal unitPrice;
    }
}
