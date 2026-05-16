package com.erp.accounting.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_entry", indexes = {
        @Index(name = "idx_je_company", columnList = "companyId"),
        @Index(name = "idx_je_no", columnList = "entryNo"),
        @Index(name = "idx_je_date", columnList = "entryDate")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalEntry extends BaseEntity {

    public enum EntryType { PURCHASE, SALES, GENERAL, ADJUST }
    public enum EntryStatus { DRAFT, POSTED, CANCELLED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 30)
    private String entryNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private EntryType entryType;

    @Column(length = 30)
    private String refType;

    private Long refId;

    @Column(nullable = false)
    private LocalDate entryDate;

    @Column(name = "is_auto", nullable = false)
    @Builder.Default
    private boolean auto = false;

    public boolean isAuto() { return auto; }

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private EntryStatus status = EntryStatus.DRAFT;

    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalDebit = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    @Builder.Default
    private BigDecimal totalCredit = BigDecimal.ZERO;

    @Column(length = 500)
    private String description;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JournalEntryLine> lines = new ArrayList<>();

    public void post() {
        if (status != EntryStatus.DRAFT) throw new IllegalStateException("DRAFT 상태에서만 전기 가능합니다.");
        validateBalance();
        this.status = EntryStatus.POSTED;
    }

    public void cancel() {
        if (status == EntryStatus.CANCELLED) throw new IllegalStateException("이미 취소된 전표입니다.");
        this.status = EntryStatus.CANCELLED;
    }

    public void calculateTotals() {
        this.totalDebit = lines.stream()
                .map(l -> l.getDebitAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.totalCredit = lines.stream()
                .map(l -> l.getCreditAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateBalance() {
        calculateTotals();
        if (totalDebit.compareTo(totalCredit) != 0) {
            throw new IllegalStateException("차변 합계(" + totalDebit + ")와 대변 합계(" + totalCredit + ")가 일치하지 않습니다.");
        }
    }
}
