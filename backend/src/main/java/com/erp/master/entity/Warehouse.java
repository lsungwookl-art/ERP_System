package com.erp.master.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "warehouse")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Warehouse extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 100)
    private String warehouseName;

    @Column(length = 200)
    private String address;

    @Column(length = 200)
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;

    public void update(String warehouseName, String address, String description) {
        this.warehouseName = warehouseName;
        this.address = address;
        this.description = description;
    }
}
