package com.erp.accounting.service;

import com.erp.accounting.dto.LedgerLineDto;
import com.erp.accounting.dto.LedgerResponse;
import com.erp.accounting.dto.TrialBalanceRowDto;
import com.erp.accounting.entity.Account;
import com.erp.accounting.entity.JournalEntryLine;
import com.erp.accounting.repository.AccountRepository;
import com.erp.accounting.repository.JournalEntryLineRepository;
import com.erp.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LedgerService {

    private final JournalEntryLineRepository lineRepository;
    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public LedgerResponse getLedger(Long companyId, String accountCode,
                                     LocalDate fromDate, LocalDate toDate) {
        Account account = accountRepository
                .findByCompanyIdAndAccountCodeAndDeletedFalse(companyId, accountCode)
                .orElseThrow(() -> BusinessException.notFound("계정과목(" + accountCode + ")"));

        BigDecimal openingBalance = fromDate != null
                ? lineRepository.sumBalanceBefore(companyId, accountCode, fromDate)
                : BigDecimal.ZERO;

        List<JournalEntryLine> lines = lineRepository.findByAccount(companyId, accountCode, fromDate, toDate);

        List<LedgerLineDto> result = new ArrayList<>();
        BigDecimal running = openingBalance;

        for (JournalEntryLine l : lines) {
            running = running.add(l.getDebitAmount()).subtract(l.getCreditAmount());
            result.add(new LedgerLineDto(
                    l.getJournalEntry().getEntryDate(),
                    l.getJournalEntry().getEntryNo(),
                    l.getDescription(),
                    l.getDebitAmount(),
                    l.getCreditAmount(),
                    running
            ));
        }

        return new LedgerResponse(accountCode, account.getAccountName(),
                openingBalance, result, running);
    }

    @Transactional(readOnly = true)
    public List<TrialBalanceRowDto> getTrialBalance(Long companyId,
                                                     LocalDate fromDate, LocalDate toDate) {
        List<Object[]> rows = lineRepository.findTrialBalance(companyId, fromDate, toDate);

        Map<String, Account> accountMap = accountRepository
                .findByCompanyIdAndDeletedFalseOrderByAccountCode(companyId)
                .stream()
                .collect(Collectors.toMap(Account::getAccountCode, a -> a));

        return rows.stream().map(row -> {
            String code = (String) row[0];
            String name = (String) row[1];
            BigDecimal totalDebit = (BigDecimal) row[2];
            BigDecimal totalCredit = (BigDecimal) row[3];

            Account acc = accountMap.get(code);
            String type = acc != null ? acc.getAccountType().name() : "UNKNOWN";

            boolean creditNormal = acc != null && (
                    acc.getAccountType() == Account.AccountType.LIABILITY ||
                    acc.getAccountType() == Account.AccountType.EQUITY ||
                    acc.getAccountType() == Account.AccountType.REVENUE);

            BigDecimal balance = creditNormal
                    ? totalCredit.subtract(totalDebit)
                    : totalDebit.subtract(totalCredit);

            return new TrialBalanceRowDto(code, name, type, totalDebit, totalCredit, balance);
        }).collect(Collectors.toList());
    }
}
