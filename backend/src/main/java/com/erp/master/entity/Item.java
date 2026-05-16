package com.erp.master.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "item", indexes = {
        @Index(name = "idx_item_company", columnList = "companyId"),
        @Index(name = "idx_item_code", columnList = "companyId, itemCode")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Item extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 50)
    private String itemCode;

    @Column(nullable = false, length = 200)
    private String itemName;

    private Long categoryId;

    @Column(length = 20)
    private String unit;

    @Column(precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal standardPrice = BigDecimal.ZERO;

    @Column(precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal standardCost = BigDecimal.ZERO;

    @Column(precision = 18, scale = 4)
    @Builder.Default
    private BigDecimal safetyStock = BigDecimal.ZERO;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public void update(String itemName, String unit, BigDecimal standardPrice,
                       BigDecimal standardCost, BigDecimal safetyStock, String description, Long categoryId) {
        this.itemName = itemName;
        this.unit = unit;
        this.standardPrice = standardPrice;
        this.standardCost = standardCost;
        this.safetyStock = safetyStock;
        this.description = description;
        this.categoryId = categoryId;
    }
}
