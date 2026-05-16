package com.erp.accounting.repository;

import com.erp.accounting.entity.JournalEntryLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface JournalEntryLineRepository extends JpaRepository<JournalEntryLine, Long> {

    @Query("SELECT l FROM JournalEntryLine l JOIN FETCH l.journalEntry j " +
           "WHERE j.companyId = :companyId AND j.deleted = false AND j.status = 'POSTED' " +
           "AND l.accountCode = :accountCode " +
           "AND (:fromDate IS NULL OR j.entryDate >= :fromDate) " +
           "AND (:toDate IS NULL OR j.entryDate <= :toDate) " +
           "ORDER BY j.entryDate ASC, j.entryNo ASC")
    List<JournalEntryLine> findByAccount(@Param("companyId") Long companyId,
                                          @Param("accountCode") String accountCode,
                                          @Param("fromDate") LocalDate fromDate,
                                          @Param("toDate") LocalDate toDate);

    @Query("SELECT COALESCE(SUM(l.debitAmount) - SUM(l.creditAmount), 0) " +
           "FROM JournalEntryLine l JOIN l.journalEntry j " +
           "WHERE j.companyId = :companyId AND j.deleted = false AND j.status = 'POSTED' " +
           "AND l.accountCode = :accountCode AND j.entryDate < :fromDate")
    BigDecimal sumBalanceBefore(@Param("companyId") Long companyId,
                                 @Param("accountCode") String accountCode,
                                 @Param("fromDate") LocalDate fromDate);

    @Query("SELECT l.accountCode, l.accountName, SUM(l.debitAmount), SUM(l.creditAmount) " +
           "FROM JournalEntryLine l JOIN l.journalEntry j " +
           "WHERE j.companyId = :companyId AND j.deleted = false AND j.status = 'POSTED' " +
           "AND (:fromDate IS NULL OR j.entryDate >= :fromDate) " +
           "AND (:toDate IS NULL OR j.entryDate <= :toDate) " +
           "GROUP BY l.accountCode, l.accountName ORDER BY l.accountCode")
    List<Object[]> findTrialBalance(@Param("companyId") Long companyId,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate);
}
