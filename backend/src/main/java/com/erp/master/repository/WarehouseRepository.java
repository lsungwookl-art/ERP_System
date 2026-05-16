package com.erp.master.repository;

import com.erp.master.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {
    Optional<Warehouse> findByIdAndCompanyIdAndDeletedFalse(Long id, Long companyId);
    List<Warehouse> findByCompanyIdAndDeletedFalse(Long companyId);
}
