package com.erp.master.repository;

import com.erp.master.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ItemRepository extends JpaRepository<Item, Long> {
    Optional<Item> findByIdAndCompanyIdAndDeletedFalse(Long id, Long companyId);

    boolean existsByItemCodeAndCompanyIdAndDeletedFalse(String itemCode, Long companyId);

    @Query("SELECT i FROM Item i WHERE i.companyId = :companyId AND i.deleted = false " +
           "AND (:keyword IS NULL OR i.itemName LIKE %:keyword% OR i.itemCode LIKE %:keyword%)")
    Page<Item> findByCompanyIdAndKeyword(@Param("companyId") Long companyId,
                                          @Param("keyword") String keyword,
                                          Pageable pageable);
}
