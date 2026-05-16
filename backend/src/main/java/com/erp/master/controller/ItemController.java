package com.erp.master.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.security.SecurityUtils;
import com.erp.master.dto.ItemRequest;
import com.erp.master.entity.Item;
import com.erp.master.service.ItemService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Item>>> list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable) {
        Long companyId = SecurityUtils.currentCompanyId();
        return ResponseEntity.ok(ApiResponse.ok(new PageResponse<>(itemService.getList(companyId, keyword, pageable))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> get(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.getById(SecurityUtils.currentCompanyId(), id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Item>> create(@Valid @RequestBody ItemRequest req) {
        Item item = itemService.create(SecurityUtils.currentCompanyId(), req);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok(item, "품목이 등록되었습니다."));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Item>> update(@PathVariable Long id, @Valid @RequestBody ItemRequest req) {
        return ResponseEntity.ok(ApiResponse.ok(itemService.update(SecurityUtils.currentCompanyId(), id, req)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        itemService.delete(SecurityUtils.currentCompanyId(), id);
        return ResponseEntity.ok(ApiResponse.ok(null, "삭제되었습니다."));
    }
}
