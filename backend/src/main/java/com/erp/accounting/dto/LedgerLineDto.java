package com.erp.accounting.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LedgerLineDto(
        LocalDate entryDate,
        String entryNo,
        String description,
        BigDecimal debit,
        BigDecimal credit,
        BigDecimal balance
) {}
