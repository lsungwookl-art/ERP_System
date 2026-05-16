package com.erp.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_balance",
        uniqueConstraints = @UniqueConstraint(columnNames = {"companyId", "itemId", "warehouseId"}),
        indexes = @Index(name = "idx_stock_company_item", columnList = "companyId, itemId"))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockBalance {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal qty = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal avgCost = BigDecimal.ZERO;

    @Version
    private Integer version;

    private LocalDateTime lastMovedAt;

    public void receiveStock(BigDecimal inQty, BigDecimal inUnitPrice) {
        BigDecimal existingAmount = this.qty.multiply(this.avgCost);
        BigDecimal inAmount = inQty.multiply(inUnitPrice);
        BigDecimal newQty = this.qty.add(inQty);

        if (newQty.compareTo(BigDecimal.ZERO) > 0) {
            this.avgCost = existingAmount.add(inAmount).divide(newQty, 4, RoundingMode.HALF_UP);
        }
        this.qty = newQty;
        this.lastMovedAt = LocalDateTime.now();
    }

    public void issueStock(BigDecimal outQty) {
        if (this.qty.compareTo(outQty) < 0) {
            throw new IllegalStateException("재고 부족: 현재고 " + this.qty + ", 출고 요청 " + outQty);
        }
        this.qty = this.qty.subtract(outQty);
        this.lastMovedAt = LocalDateTime.now();
    }

    public void adjustStock(BigDecimal newQty, BigDecimal newCost) {
        this.qty = newQty;
        if (newCost != null) this.avgCost = newCost;
        this.lastMovedAt = LocalDateTime.now();
    }
}
