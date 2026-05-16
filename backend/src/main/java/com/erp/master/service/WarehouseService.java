package com.erp.master.service;

import com.erp.common.exception.BusinessException;
import com.erp.master.dto.WarehouseRequest;
import com.erp.master.entity.Warehouse;
import com.erp.master.repository.WarehouseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;

    @Transactional
    public Warehouse create(Long companyId, WarehouseRequest req) {
        return warehouseRepository.save(Warehouse.builder()
                .companyId(companyId)
                .warehouseName(req.getWarehouseName())
                .address(req.getAddress())
                .description(req.getDescription())
                .build());
    }

    @Transactional(readOnly = true)
    public List<Warehouse> getList(Long companyId) {
        return warehouseRepository.findByCompanyIdAndDeletedFalse(companyId);
    }

    @Transactional(readOnly = true)
    public Warehouse getById(Long companyId, Long id) {
        return warehouseRepository.findByIdAndCompanyIdAndDeletedFalse(id, companyId)
                .orElseThrow(() -> BusinessException.notFound("창고"));
    }

    @Transactional
    public Warehouse update(Long companyId, Long id, WarehouseRequest req) {
        Warehouse wh = getById(companyId, id);
        wh.update(req.getWarehouseName(), req.getAddress(), req.getDescription());
        return wh;
    }

    @Transactional
    public void delete(Long companyId, Long id) {
        Warehouse wh = getById(companyId, id);
        wh.softDelete();
    }
}
