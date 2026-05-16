package com.erp.inventory.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movement", indexes = {
        @Index(name = "idx_movement_company_item", columnList = "companyId, itemId"),
        @Index(name = "idx_movement_date", columnList = "movementDate")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovement {

    public enum MovementType { IN, OUT, ADJUST, TRANSFER }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long warehouseId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private MovementType movementType;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal qty;

    @Column(precision = 18, scale = 4)
    private BigDecimal unitCost;

    @Column(precision = 18, scale = 4)
    private BigDecimal beforeQty;

    @Column(precision = 18, scale = 4)
    private BigDecimal afterQty;

    @Column(length = 20)
    private String refType;

    private Long refId;

    @Column(length = 200)
    private String remark;

    @Column(nullable = false)
    private LocalDate movementDate;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(length = 100)
    private String createdBy;
}
