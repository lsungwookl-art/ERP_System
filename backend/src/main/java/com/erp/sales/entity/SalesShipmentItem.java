package com.erp.sales.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_shipment_item")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesShipmentItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_shipment_id", nullable = false)
    private SalesShipment salesShipment;

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal qty;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;

    @Column(precision = 18, scale = 4)
    private BigDecimal avgCostAtShipment;

    public void setAvgCost(BigDecimal avgCost) {
        this.avgCostAtShipment = avgCost;
    }
}
