package com.erp.bom.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "bom", indexes = {
        @Index(name = "idx_bom_parent", columnList = "companyId, parentItemId")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Bom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false)
    private Long parentItemId;

    @Column(nullable = false)
    private Long childItemId;

    @Column(nullable = false, precision = 18, scale = 4)
    private BigDecimal qty;

    @Column(length = 20)
    private String unit;

    @Column(length = 300)
    private String remark;

    public void update(BigDecimal qty, String unit, String remark) {
        this.qty = qty;
        this.unit = unit;
        this.remark = remark;
    }
}
