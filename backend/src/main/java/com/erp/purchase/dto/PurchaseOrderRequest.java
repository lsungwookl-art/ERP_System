package com.erp.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class PurchaseOrderRequest {
    @NotNull private Long partnerId;
    @NotNull private LocalDate orderDate;
    private LocalDate expectedDate;
    private String remark;

    @NotEmpty @Valid
    private List<OrderItemDto> items;

    @Getter
    public static class OrderItemDto {
        @NotNull private Long itemId;
        @NotNull private BigDecimal orderQty;
        @NotNull private BigDecimal unitPrice;
    }
}
