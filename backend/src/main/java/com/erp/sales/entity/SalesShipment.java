package com.erp.sales.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_shipment", indexes = {
        @Index(name = "idx_ss_company", columnList = "companyId"),
        @Index(name = "idx_ss_no", columnList = "shipmentNo")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesShipment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 30)
    private String shipmentNo;

    @Column(nullable = false)
    private Long salesOrderId;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private LocalDate shipmentDate;

    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private Long journalEntryId;

    @Column(length = 500)
    private String remark;

    @OneToMany(mappedBy = "salesShipment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SalesShipmentItem> items = new ArrayList<>();

    public void linkJournalEntry(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(i -> i.getQty().multiply(i.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
