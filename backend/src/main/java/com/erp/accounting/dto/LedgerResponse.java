package com.erp.accounting.dto;

import java.math.BigDecimal;
import java.util.List;

public record LedgerResponse(
        String accountCode,
        String accountName,
        BigDecimal openingBalance,
        List<LedgerLineDto> lines,
        BigDecimal closingBalance
) {}
