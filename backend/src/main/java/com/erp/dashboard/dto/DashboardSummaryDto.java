package com.erp.dashboard.dto;

public record DashboardSummaryDto(
        long totalItems,
        long stockAlertCount,
        long pendingPurchaseOrders,
        long pendingSalesOrders,
        long pendingApprovals,
        long totalJournalEntries
) {}
