package com.erp.accounting.controller;

import com.erp.accounting.dto.LedgerResponse;
import com.erp.accounting.dto.TrialBalanceRowDto;
import com.erp.accounting.entity.Account;
import com.erp.accounting.entity.JournalEntry;
import com.erp.accounting.repository.JournalEntryRepository;
import com.erp.accounting.service.JournalEntryService;
import com.erp.accounting.service.LedgerService;
import com.erp.common.dto.ApiResponse;
import com.erp.common.dto.PageResponse;
import com.erp.common.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/accounting")
@RequiredArgsConstructor
public class AccountingController {

    private final JournalEntryRepository journalEntryRepository;
    private final JournalEntryService journalEntryService;
    private final LedgerService ledgerService;

    @GetMapping("/journals")
    public ResponseEntity<ApiResponse<PageResponse<JournalEntry>>> journals(
            @RequestParam(required = false) JournalEntry.EntryType entryType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @PageableDefault(size = 20) Pageable pageable) {
        Long companyId = SecurityUtils.currentCompanyId();
        return ResponseEntity.ok(ApiResponse.ok(new PageResponse<>(
                journalEntryRepository.findByFilter(companyId, entryType, fromDate, toDate, pageable))));
    }

    @GetMapping("/accounts")
    public ResponseEntity<ApiResponse<List<Account>>> accounts() {
        return ResponseEntity.ok(ApiResponse.ok(
                journalEntryService.getAccounts(SecurityUtils.currentCompanyId())));
    }

    @GetMapping("/ledger")
    public ResponseEntity<ApiResponse<LedgerResponse>> ledger(
            @RequestParam String accountCode,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.ok(
                ledgerService.getLedger(SecurityUtils.currentCompanyId(), accountCode, fromDate, toDate)));
    }

    @GetMapping("/trial-balance")
    public ResponseEntity<ApiResponse<List<TrialBalanceRowDto>>> trialBalance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate) {
        return ResponseEntity.ok(ApiResponse.ok(
                ledgerService.getTrialBalance(SecurityUtils.currentCompanyId(), fromDate, toDate)));
    }
}
