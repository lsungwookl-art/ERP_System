package com.erp.purchase.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_order", indexes = {
        @Index(name = "idx_po_company", columnList = "companyId"),
        @Index(name = "idx_po_no", columnList = "orderNo")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder extends BaseEntity {

    public enum OrderStatus { DRAFT, CONFIRMED, PARTIALLY_RECEIVED, COMPLETED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 30)
    private String orderNo;

    @Column(nullable = false)
    private Long partnerId;

    @Column(nullable = false)
    private LocalDate orderDate;

    private LocalDate expectedDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private OrderStatus status = OrderStatus.DRAFT;

    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 500)
    private String remark;

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public void confirm() {
        if (status != OrderStatus.DRAFT) throw new IllegalStateException("DRAFT 상태에서만 확정 가능합니다.");
        this.status = OrderStatus.CONFIRMED;
    }

    public void complete() {
        this.status = OrderStatus.COMPLETED;
    }

    public void cancel() {
        if (status == OrderStatus.COMPLETED) throw new IllegalStateException("완료된 발주는 취소할 수 없습니다.");
        this.status = OrderStatus.CANCELLED;
    }

    public void calculateTotal() {
        this.totalAmount = items.stream()
                .map(PurchaseOrderItem::getLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
