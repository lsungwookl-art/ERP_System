package com.erp.accounting.service;

import com.erp.accounting.entity.Account;
import com.erp.accounting.entity.JournalEntry;
import com.erp.accounting.entity.JournalEntry.EntryType;
import com.erp.accounting.entity.JournalEntryLine;
import com.erp.accounting.entity.JournalEntryLine.LineType;
import com.erp.accounting.repository.AccountRepository;
import com.erp.accounting.repository.JournalEntryRepository;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.DocSequenceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JournalEntryService {

    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;
    private final DocSequenceService docSequenceService;

    // 구매 입고 자동분개: 차) 재고자산 / 대) 매입채무
    @Transactional
    public JournalEntry createPurchaseJournal(Long companyId, Long receiptId,
                                               BigDecimal amount, LocalDate entryDate) {
        Account inventoryAccount = getAccount(companyId, "1300");  // 재고자산
        Account payableAccount = getAccount(companyId, "2100");    // 매입채무

        JournalEntry entry = buildEntry(companyId, EntryType.PURCHASE, "PURCHASE_RECEIPT",
                receiptId, entryDate, "매입 입고 자동분개");

        entry.getLines().add(buildLine(entry, inventoryAccount, LineType.DEBIT, amount, "재고자산 증가"));
        entry.getLines().add(buildLine(entry, payableAccount, LineType.CREDIT, amount, "매입채무 발생"));

        entry.calculateTotals();
        entry.post();
        return journalEntryRepository.save(entry);
    }

    // 판매 출고 자동분개: 차) 매출채권 / 대) 상품매출 + 차) 매출원가 / 대) 재고자산
    @Transactional
    public JournalEntry createSalesJournal(Long companyId, Long shipmentId,
                                            BigDecimal salesAmount, BigDecimal costAmount,
                                            LocalDate entryDate) {
        Account receivableAccount = getAccount(companyId, "1200");   // 매출채권
        Account revenueAccount = getAccount(companyId, "4100");      // 상품매출
        Account cogsAccount = getAccount(companyId, "5100");         // 매출원가
        Account inventoryAccount = getAccount(companyId, "1300");    // 재고자산

        JournalEntry entry = buildEntry(companyId, EntryType.SALES, "SALES_SHIPMENT",
                shipmentId, entryDate, "판매 출고 자동분개");

        entry.getLines().add(buildLine(entry, receivableAccount, LineType.DEBIT, salesAmount, "매출채권 발생"));
        entry.getLines().add(buildLine(entry, revenueAccount, LineType.CREDIT, salesAmount, "상품매출 인식"));
        entry.getLines().add(buildLine(entry, cogsAccount, LineType.DEBIT, costAmount, "매출원가 인식"));
        entry.getLines().add(buildLine(entry, inventoryAccount, LineType.CREDIT, costAmount, "재고자산 감소"));

        entry.calculateTotals();
        entry.post();
        return journalEntryRepository.save(entry);
    }

    private JournalEntry buildEntry(Long companyId, EntryType type, String refType,
                                     Long refId, LocalDate entryDate, String description) {
        String entryNo = docSequenceService.next(companyId, "JE");
        return JournalEntry.builder()
                .companyId(companyId)
                .entryNo(entryNo)
                .entryType(type)
                .refType(refType)
                .refId(refId)
                .entryDate(entryDate)
                .auto(true)
                .description(description)
                .build();
    }

    private JournalEntryLine buildLine(JournalEntry entry, Account account,
                                        LineType lineType, BigDecimal amount, String desc) {
        return JournalEntryLine.builder()
                .journalEntry(entry)
                .accountId(account.getId())
                .accountCode(account.getAccountCode())
                .accountName(account.getAccountName())
                .lineType(lineType)
                .debitAmount(lineType == LineType.DEBIT ? amount : BigDecimal.ZERO)
                .creditAmount(lineType == LineType.CREDIT ? amount : BigDecimal.ZERO)
                .description(desc)
                .build();
    }

    private Account getAccount(Long companyId, String code) {
        return accountRepository.findByCompanyIdAndAccountCodeAndDeletedFalse(companyId, code)
                .orElseThrow(() -> BusinessException.notFound("계정과목(" + code + ")"));
    }

    @Transactional(readOnly = true)
    public List<Account> getAccounts(Long companyId) {
        return accountRepository.findByCompanyIdAndDeletedFalseOrderByAccountCode(companyId);
    }
}
