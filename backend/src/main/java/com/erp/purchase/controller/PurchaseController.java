package com.erp.purchase.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.security.SecurityUtils;
import com.erp.purchase.dto.PurchaseOrderRequest;
import com.erp.purchase.dto.PurchaseReceiptRequest;
import com.erp.purchase.entity.PurchaseOrder;
import com.erp.purchase.entity.PurchaseReceipt;
import com.erp.purchase.service.PurchaseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/purchase")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseOrder>>> listOrders(
            @PageableDefault(size = 20) Pageable pageable) {
        Long companyId = SecurityUtils.currentCompanyId();
        return ResponseEntity.ok(ApiResponse.ok(new PageResponse<>(purchaseService.getOrders(companyId, pageable))));
    }

    @GetMapping("/orders/{id}")
    public ResponseEntity<ApiResponse<PurchaseOrder>> getOrder(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(purchaseService.getOrder(SecurityUtils.currentCompanyId(), id)));
    }

    @PostMapping("/orders")
    public ResponseEntity<ApiResponse<PurchaseOrder>> createOrder(@Valid @RequestBody PurchaseOrderRequest req) {
        Long companyId = SecurityUtils.currentCompanyId();
        PurchaseOrder order = purchaseService.createOrder(companyId, req, SecurityUtils.currentEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(order, "발주서가 등록되었습니다."));
    }

    @PatchMapping("/orders/{id}/confirm")
    public ResponseEntity<ApiResponse<PurchaseOrder>> confirmOrder(@PathVariable Long id) {
        PurchaseOrder order = purchaseService.confirmOrder(SecurityUtils.currentCompanyId(), id);
        return ResponseEntity.ok(ApiResponse.ok(order, "발주서가 확정되었습니다."));
    }

    @GetMapping("/receipts")
    public ResponseEntity<ApiResponse<PageResponse<PurchaseReceipt>>> listReceipts(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(
                new PageResponse<>(purchaseService.getReceipts(SecurityUtils.currentCompanyId(), pageable))));
    }

    @PostMapping("/receipts")
    public ResponseEntity<ApiResponse<PurchaseReceipt>> createReceipt(@Valid @RequestBody PurchaseReceiptRequest req) {
        Long companyId = SecurityUtils.currentCompanyId();
        PurchaseReceipt receipt = purchaseService.createReceipt(companyId, req, SecurityUtils.currentEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(receipt, "입고가 처리되었습니다."));
    }
}
