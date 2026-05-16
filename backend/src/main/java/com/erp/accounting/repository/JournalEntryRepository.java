package com.erp.accounting.repository;

import com.erp.accounting.entity.JournalEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {

    @Query("SELECT j FROM JournalEntry j WHERE j.companyId = :companyId " +
           "AND (:entryType IS NULL OR j.entryType = :entryType) " +
           "AND (:fromDate IS NULL OR j.entryDate >= :fromDate) " +
           "AND (:toDate IS NULL OR j.entryDate <= :toDate) " +
           "AND j.deleted = false ORDER BY j.entryDate DESC")
    Page<JournalEntry> findByFilter(@Param("companyId") Long companyId,
                                     @Param("entryType") JournalEntry.EntryType entryType,
                                     @Param("fromDate") LocalDate fromDate,
                                     @Param("toDate") LocalDate toDate,
                                     Pageable pageable);
}
