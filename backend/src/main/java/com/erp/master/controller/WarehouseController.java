package com.erp.master.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.security.SecurityUtils;
import com.erp.master.dto.WarehouseRequest;
import com.erp.master.entity.Warehouse;
import com.erp.master.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Warehouse>>> list() {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getList(SecurityUtils.currentCompanyId())));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Warehouse>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.getById(SecurityUtils.currentCompanyId(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Warehouse>> create(@Valid @RequestBody WarehouseRequest req) {
        Warehouse wh = warehouseService.create(SecurityUtils.currentCompanyId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(wh, "창고가 등록되었습니다."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Warehouse>> update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(warehouseService.update(SecurityUtils.currentCompanyId(), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        warehouseService.delete(SecurityUtils.currentCompanyId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null, "삭제되었습니다."));
    }
}
