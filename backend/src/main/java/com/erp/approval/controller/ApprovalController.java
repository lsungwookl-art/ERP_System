package com.erp.approval.controller;

import com.erp.approval.dto.ApprovalActionRequest;
import com.erp.approval.dto.ApprovalCreateRequest;
import com.erp.approval.entity.ApprovalRequest;
import com.erp.approval.service.ApprovalService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ApprovalRequest>>> list(
            @RequestParam(required = false) ApprovalRequest.ApprovalStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.ok(new PageResponse<>(
                approvalService.list(SecurityUtils.currentCompanyId(), status, pageable))));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ApprovalRequest>> create(
            @Valid @RequestBody ApprovalCreateRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                approvalService.create(SecurityUtils.currentCompanyId(),
                        SecurityUtils.currentUserId(), req)));
    }

    @PostMapping("/{id}/approve")
    public ResponseEntity<ApiResponse<ApprovalRequest>> approve(
            @PathVariable Long id, @RequestBody ApprovalActionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                approvalService.approve(SecurityUtils.currentCompanyId(), id,
                        SecurityUtils.currentUserId(), req.getComment())));
    }

    @PostMapping("/{id}/reject")
    public ResponseEntity<ApiResponse<ApprovalRequest>> reject(
            @PathVariable Long id, @RequestBody ApprovalActionRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(
                approvalService.reject(SecurityUtils.currentCompanyId(), id,
                        SecurityUtils.currentUserId(), req.getComment())));
    }
}
