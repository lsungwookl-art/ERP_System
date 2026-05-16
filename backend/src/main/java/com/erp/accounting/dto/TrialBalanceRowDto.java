package com.erp.accounting.dto;

import java.math.BigDecimal;

public record TrialBalanceRowDto(
        String accountCode,
        String accountName,
        String accountType,
        BigDecimal totalDebit,
        BigDecimal totalCredit,
        BigDecimal balance
) {}
