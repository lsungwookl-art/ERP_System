package com.erp.master.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.security.SecurityUtils;
import com.erp.master.dto.PartnerRequest;
import com.erp.master.entity.Partner;
import com.erp.master.service.PartnerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/partners")
@RequiredArgsConstructor
public class PartnerController {

    private final PartnerService partnerService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Partner>>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Partner.PartnerType type,
            @PageableDefault(size = 20) Pageable pageable) {
        Long companyId = SecurityUtils.currentCompanyId();
        return ResponseEntity.ok(ApiResponse.ok(
                new PageResponse<>(partnerService.getList(companyId, keyword, type, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Partner>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(partnerService.getById(SecurityUtils.currentCompanyId(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Partner>> create(@Valid @RequestBody PartnerRequest req) {
        Partner partner = partnerService.create(SecurityUtils.currentCompanyId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(partner, "거래처가 등록되었습니다."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Partner>> update(@PathVariable Long id, @Valid @RequestBody PartnerRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(partnerService.update(SecurityUtils.currentCompanyId(), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        partnerService.delete(SecurityUtils.currentCompanyId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null, "삭제되었습니다."));
    }
}
