package com.erp.common.config;

import com.erp.master.entity.Company;
import com.erp.master.entity.User;
import com.erp.master.entity.Warehouse;
import com.erp.master.repository.CompanyRepository;
import com.erp.master.repository.UserRepository;
import com.erp.master.repository.WarehouseRepository;
import com.erp.accounting.entity.Account;
import com.erp.accounting.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements ApplicationRunner {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final WarehouseRepository warehouseRepository;
    private final AccountRepository accountRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (companyRepository.count() > 0) {
            log.info("초기 데이터가 이미 존재합니다. 건너뜁니다.");
            return;
        }

        log.info("초기 데이터를 생성합니다...");

        Company company = companyRepository.save(Company.builder()
                .name("(주)테스트컴퍼니")
                .businessNo("123-45-67890")
                .address("서울시 강남구 테헤란로 123")
                .representativeName("홍길동")
                .active(true)
                .build());

        userRepository.save(User.builder()
                .companyId(company.getId())
                .email("admin@test.com")
                .password(passwordEncoder.encode("password123"))
                .name("관리자")
                .phone("010-1234-5678")
                .active(true)
                .build());

        warehouseRepository.save(Warehouse.builder()
                .companyId(company.getId())
                .warehouseName("본사 창고")
                .address("서울시 강남구")
                .description("기본 창고")
                .active(true)
                .build());

        Long cid = company.getId();
        String[][] accounts = {
            {"1000","유동자산","ASSET",null},
            {"1200","매출채권","ASSET","1000"},
            {"1300","재고자산","ASSET","1000"},
            {"2000","유동부채","LIABILITY",null},
            {"2100","매입채무","LIABILITY","2000"},
            {"3000","자본","EQUITY",null},
            {"4000","매출","REVENUE",null},
            {"4100","상품매출","REVENUE","4000"},
            {"5000","매출원가","EXPENSE",null},
            {"5100","매출원가(상품)","EXPENSE","5000"},
        };

        for (String[] a : accounts) {
            Long parentId = null;
            if (a[3] != null) {
                parentId = accountRepository.findByCompanyIdAndAccountCodeAndDeletedFalse(cid, a[3])
                        .map(Account::getId).orElse(null);
            }
            accountRepository.save(Account.builder()
                    .companyId(cid)
                    .accountCode(a[0])
                    .accountName(a[1])
                    .accountType(Account.AccountType.valueOf(a[2]))
                    .parentId(parentId)
                    .active(true)
                    .build());
        }

        log.info("초기 데이터 생성 완료.");
    }
}
