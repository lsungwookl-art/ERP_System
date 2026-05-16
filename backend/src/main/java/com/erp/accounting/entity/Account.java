package com.erp.accounting.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account", indexes = {
        @Index(name = "idx_account_company", columnList = "companyId"),
        @Index(name = "idx_account_code", columnList = "companyId, accountCode")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account extends BaseEntity {

    public enum AccountType { ASSET, LIABILITY, EQUITY, REVENUE, EXPENSE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 10)
    private String accountCode;

    @Column(nullable = false, length = 100)
    private String accountName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private AccountType accountType;

    private Long parentId;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
