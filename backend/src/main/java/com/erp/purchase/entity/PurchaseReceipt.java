package com.erp.purchase.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_receipt", indexes = {
        @Index(name = "idx_pr_company", columnList = "companyId"),
        @Index(name = "idx_pr_no", columnList = "receiptNo")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseReceipt extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 30)
    private String receiptNo;

    @Column(nullable = false)
    private Long purchaseOrderId;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private Long warehouseId;

    @Column(nullable = false)
    private LocalDate receiptDate;

    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private Long journalEntryId;

    @Column(length = 500)
    private String remark;

    @OneToMany(mappedBy = "purchaseReceipt", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseReceiptItem> items = new ArrayList<>();

    public void linkJournalEntry(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(i -> i.getQty().multiply(i.getUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
