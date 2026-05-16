package com.erp.purchase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_order_item")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal orderQty;

    @Column(precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal receivedQty = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(precision = 18, scale = 2)
    private BigDecimal lineAmount;

    @PrePersist
    @PreUpdate
    private void calcLine() {
        this.lineAmount = orderQty.multiply(unitPrice);
    }

    public BigDecimal getLineAmount() {
        return orderQty.multiply(unitPrice);
    }

    public void addReceivedQty(BigDecimal qty) {
        this.receivedQty = this.receivedQty.add(qty);
    }
}
