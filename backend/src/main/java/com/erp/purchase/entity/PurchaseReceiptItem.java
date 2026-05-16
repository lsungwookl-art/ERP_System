package com.erp.purchase.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "purchase_receipt_item")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_receipt_id", nullable = false)
    private PurchaseReceipt purchaseReceipt;

    @Column(nullable = false)
    private Long orderItemId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal qty;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal unitPrice;
}
