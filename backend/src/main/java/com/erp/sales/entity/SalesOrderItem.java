package com.erp.sales.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_item")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal orderQty;

    @Column(precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal shippedQty = BigDecimal.ZERO;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;

    public BigDecimal getLineAmount() {
        return orderQty.multiply(unitPrice);
    }

    public void addShippedQty(BigDecimal qty) {
        this.shippedQty = this.shippedQty.add(qty);
    }
}
