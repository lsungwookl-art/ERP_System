package com.erp.accounting.repository;

import com.erp.accounting.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCompanyIdAndAccountCodeAndDeletedFalse(Long companyId, String accountCode);
    List<Account> findByCompanyIdAndDeletedFalseOrderByAccountCode(Long companyId);
}
