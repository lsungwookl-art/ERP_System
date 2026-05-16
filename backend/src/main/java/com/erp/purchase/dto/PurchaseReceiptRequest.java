package com.erp.purchase.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
public class PurchaseReceiptRequest {
    @NotNull private Long orderId;
    @NotNull private Long warehouseId;
    @NotNull private LocalDate receiptDate;
    private String remark;

    @NotEmpty @Valid
    private List<ReceiptItemDto> items;

    @Getter
    public static class ReceiptItemDto {
        @NotNull private Long orderItemId;
        @NotNull private Long itemId;
        @NotNull private BigDecimal qty;
        @NotNull private BigDecimal unitPrice;
    }
}
