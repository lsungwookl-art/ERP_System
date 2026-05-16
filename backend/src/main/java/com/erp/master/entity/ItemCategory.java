package com.erp.master.entity;

import com.erp.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "item_category")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemCategory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long companyId;

    @Column(nullable = false, length = 100)
    private String name;

    private Long parentId;

    private int depth;
}
