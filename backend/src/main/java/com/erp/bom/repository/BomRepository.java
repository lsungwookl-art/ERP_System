package com.erp.bom.repository;

import com.erp.bom.entity.Bom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BomRepository extends JpaRepository<Bom, Long> {

    List<Bom> findByCompanyIdAndParentItemIdAndDeletedFalseOrderByChildItemId(
            Long companyId, Long parentItemId);

    Optional<Bom> findByIdAndCompanyIdAndDeletedFalse(Long id, Long companyId);

    boolean existsByCompanyIdAndParentItemIdAndChildItemIdAndDeletedFalse(
            Long companyId, Long parentItemId, Long childItemId);
}
