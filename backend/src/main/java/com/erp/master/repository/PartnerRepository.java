package com.erp.master.repository;

import com.erp.master.entity.Partner;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {
    Optional<Partner> findByIdAndCompanyIdAndDeletedFalse(Long id, Long companyId);

    @Query("SELECT p FROM Partner p WHERE p.companyId = :companyId AND p.deleted = false " +
           "AND (:keyword IS NULL OR p.partnerName LIKE %:keyword%) " +
           "AND (:type IS NULL OR p.partnerType = :type)")
    Page<Partner> findByCompanyIdAndFilter(@Param("companyId") Long companyId,
                                            @Param("keyword") String keyword,
                                            @Param("type") Partner.PartnerType type,
                                            Pageable pageable);
}
