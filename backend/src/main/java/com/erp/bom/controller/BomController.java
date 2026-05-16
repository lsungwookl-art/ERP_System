package com.erp.bom.controller;

import com.erp.bom.dto.BomLineDto;
import com.erp.bom.dto.BomRequest;
import com.erp.bom.service.BomService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.security.SecurityUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bom")
@RequiredArgsConstructor
public class BomController {

    private final BomService bomService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<BomLineDto>>> getBom(@RequestParam Long parentItemId) {
        return ResponseEntity.ok(ApiResponse.ok(
                bomService.getBom(SecurityUtils.currentCompanyId(), parentItemId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> create(@Valid @RequestBody BomRequest req) {
        bomService.create(SecurityUtils.currentCompanyId(), req);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        bomService.delete(SecurityUtils.currentCompanyId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
