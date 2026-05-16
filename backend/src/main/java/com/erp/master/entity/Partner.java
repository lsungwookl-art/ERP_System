package com.erp.master.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "partner", indexes = {
        @Index(name = "idx_partner_company", columnList = "companyId")
})
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Partner extends BaseEntity {

    public enum PartnerType { CUSTOMER, SUPPLIER, BOTH }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 100)
    private String partnerName;

    @Column(length = 20)
    private String businessNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    @Builder.Default
    private PartnerType partnerType = PartnerType.BOTH;

    @Column(length = 50)
    private String representativeName;

    @Column(length = 200)
    private String address;

    @Column(length = 20)
    private String phone;

    @Column(length = 100)
    private String email;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public void update(String partnerName, String businessNo, PartnerType partnerType,
                       String representativeName, String address, String phone, String email) {
        this.partnerName = partnerName;
        this.businessNo = businessNo;
        this.partnerType = partnerType;
        this.representativeName = representativeName;
        this.address = address;
        this.phone = phone;
        this.email = email;
    }
}
