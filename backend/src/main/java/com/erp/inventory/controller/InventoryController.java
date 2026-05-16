package com.erp.inventory.controller;

import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.security.SecurityUtils;
import com.erp.inventory.dto.StockAdjustRequest;
import com.erp.inventory.dto.StockBalanceDto;
import com.erp.inventory.entity.StockBalance;
import com.erp.inventory.entity.StockMovement;
import com.erp.inventory.repository.StockBalanceRepository;
import com.erp.inventory.repository.StockMovementRepository;
import com.erp.inventory.service.StockService;
import com.erp.master.entity.Item;
import com.erp.master.entity.Warehouse;
import com.erp.master.repository.ItemRepository;
import com.erp.master.repository.WarehouseRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final StockBalanceRepository stockBalanceRepository;
    private final StockMovementRepository stockMovementRepository;
    private final StockService stockService;
    private final ItemRepository itemRepository;
    private final WarehouseRepository warehouseRepository;

    @GetMapping("/balances")
    public ResponseEntity<ApiResponse<PageResponse<StockBalanceDto>>> balances(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long warehouseId,
            @PageableDefault(size = 100) Pageable pageable) {
        Long companyId = SecurityUtils.currentCompanyId();

        Page<StockBalance> page = stockBalanceRepository
                .findByCompanyIdAndFilter(companyId, itemId, warehouseId, pageable);

        // 품목/창고 맵 일괄 조회
        List<Long> itemIds = page.getContent().stream().map(StockBalance::getItemId).distinct().toList();
        List<Long> warehouseIds = page.getContent().stream().map(StockBalance::getWarehouseId).distinct().toList();

        Map<Long, Item> itemMap = itemRepository.findAllById(itemIds).stream()
                .collect(Collectors.toMap(Item::getId, i -> i));
        Map<Long, Warehouse> warehouseMap = warehouseRepository.findAllById(warehouseIds).stream()
                .collect(Collectors.toMap(Warehouse::getId, w -> w));

        List<StockBalanceDto> dtos = page.getContent().stream().map(b -> {
            Item item = itemMap.get(b.getItemId());
            Warehouse warehouse = warehouseMap.get(b.getWarehouseId());
            BigDecimal safetyStock = item != null ? item.getSafetyStock() : BigDecimal.ZERO;
            BigDecimal stockValue = b.getQty().multiply(b.getAvgCost());
            boolean belowSafety = safetyStock.compareTo(BigDecimal.ZERO) > 0
                    && b.getQty().compareTo(safetyStock) < 0;
            return new StockBalanceDto(
                    b.getId(),
                    b.getItemId(),
                    item != null ? item.getItemCode() : "",
                    item != null ? item.getItemName() : "",
                    item != null ? item.getUnit() : "",
                    b.getWarehouseId(),
                    warehouse != null ? warehouse.getWarehouseName() : "",
                    b.getQty(),
                    b.getAvgCost(),
                    stockValue,
                    safetyStock,
                    belowSafety,
                    b.getLastMovedAt()
            );
        }).collect(Collectors.toList());

        Page<StockBalanceDto> dtoPage = new PageImpl<>(dtos, pageable, page.getTotalElements());
        return ResponseEntity.ok(ApiResponse.ok(new PageResponse<>(dtoPage)));
    }

    @GetMapping("/movements")
    public ResponseEntity<ApiResponse<PageResponse<StockMovement>>> movements(
            @RequestParam(required = false) Long itemId,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 50, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Long companyId = SecurityUtils.currentCompanyId();
        return ResponseEntity.ok(ApiResponse.ok(new PageResponse<>(
                stockMovementRepository.findByFilter(companyId, itemId, warehouseId, fromDate, toDate, pageable))));
    }

    @PostMapping("/adjust")
    public ResponseEntity<ApiResponse<Void>> adjust(@Valid @RequestBody StockAdjustRequest req) {
        Long companyId = SecurityUtils.currentCompanyId();
        String operator = SecurityUtils.currentEmail();
        stockService.adjustStock(companyId, req.getItemId(), req.getWarehouseId(),
                req.getNewQty(), req.getNewCost(), operator);
        return ResponseEntity.ok(ApiResponse.ok(null, "재고 조정이 완료되었습니다."));
    }
}
