package com.erp.master.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "company")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String businessNo;

    @Column(length = 200)
    private String address;

    @Column(length = 50)
    private String representativeName;

    @Column(nullable = false)
    @Builder.Default
    private boolean active = true;
}
