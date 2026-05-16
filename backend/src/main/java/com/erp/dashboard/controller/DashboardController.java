package com.erp.dashboard.controller;

import com.erp.approval.repository.ApprovalRequestRepository;
import com.erp.common.dto.ApiResponse;
import com.erp.common.security.SecurityUtils;
import com.erp.dashboard.dto.DashboardSummaryDto;
import com.erp.inventory.repository.StockBalanceRepository;
import com.erp.master.repository.ItemRepository;
import com.erp.purchase.repository.PurchaseOrderRepository;
import com.erp.sales.repository.SalesOrderRepository;
import com.erp.accounting.repository.JournalEntryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final ItemRepository itemRepository;
    private final StockBalanceRepository stockBalanceRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ApprovalRequestRepository approvalRequestRepository;
    private final JournalEntryRepository journalEntryRepository;

    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryDto>> summary() {
        Long companyId = SecurityUtils.currentCompanyId();
        Long userId = SecurityUtils.currentUserId();

        long totalItems = itemRepository.findByCompanyIdAndKeyword(companyId, null, PageRequest.of(0, 1))
                .getTotalElements();

        long stockAlerts = stockBalanceRepository
                .countStockAlerts(companyId);

        long pendingPO = purchaseOrderRepository
                .countByCompanyIdAndStatusIn(companyId,
                        java.util.List.of("DRAFT", "CONFIRMED"));

        long pendingSO = salesOrderRepository
                .countByCompanyIdAndStatusIn(companyId,
                        java.util.List.of("DRAFT", "CONFIRMED"));

        long pendingApprovals = approvalRequestRepository
                .countPendingForApprover(companyId, userId);

        long totalJournals = journalEntryRepository
                .findByFilter(companyId, null, null, null, PageRequest.of(0, 1))
                .getTotalElements();

        return ResponseEntity.ok(ApiResponse.ok(new DashboardSummaryDto(
                totalItems, stockAlerts, pendingPO, pendingSO, pendingApprovals, totalJournals)));
    }
}
