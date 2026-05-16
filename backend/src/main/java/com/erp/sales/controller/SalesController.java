package com.erp.sales.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.security.SecurityUtils;
import com.erp.sales.dto.SalesOrderRequest;
import com.erp.sales.dto.SalesShipmentRequest;
import com.erp.sales.entity.SalesOrder;
import com.erp.sales.entity.SalesShipment;
import com.erp.sales.service.SalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SalesController {

    private final SalesService salesService;

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<SalesOrder>>> listOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                new PageResponse<>(salesService.getOrders(SecurityUtils.currentCompanyId(), pageable))));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<SalesOrder>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(salesService.getOrder(SecurityUtils.currentCompanyId(), id)));
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<SalesOrder>> createOrder(@Valid @RequestBody SalesOrderRequest req) {
        Long companyId = SecurityUtils.currentCompanyId();
        SalesOrder order = salesService.createOrder(companyId, req, SecurityUtils.currentEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(order, "수주가 등록되었습니다."));
    }

    @PatchMapping("/orders/{id}/confirm")
    public ResponseEntity<ApiResponse<SalesOrder>> confirmOrder(@PathVariable Long id) {
        SalesOrder order = salesService.confirmOrder(SecurityUtils.currentCompanyId(), id);
        return ResponseEntity.ok(ApiResponse.ok(order, "수주가 확정되었습니다."));
    }

    @GetMapping("/shipments")
    public ResponseEntity<ApiResponse<PageResponse<SalesShipment>>> listShipments(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                new PageResponse<>(salesService.getShipments(SecurityUtils.currentCompanyId(), pageable))));
    }

    @PostMapping("/shipments")
    public ResponseEntity<ApiResponse<SalesShipment>> createShipment(@Valid @RequestBody SalesShipmentRequest req) {
        Long companyId = SecurityUtils.currentCompanyId();
        SalesShipment shipment = salesService.createShipment(companyId, req, SecurityUtils.currentEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(shipment, "출고가 처리되었습니다."));
    }
}
