package com.erp.master.service;

import com.erp.common.exception.BusinessException;
import com.erp.master.dto.PartnerRequest;
import com.erp.master.entity.Partner;
import com.erp.master.repository.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private final PartnerRepository partnerRepository;

    @Transactional
    public Partner create(Long companyId, PartnerRequest req) {
        return partnerRepository.save(Partner.builder()
                .companyId(companyId)
                .partnerName(req.getPartnerName())
                .businessNo(req.getBusinessNo())
                .partnerType(req.getPartnerType())
                .representativeName(req.getRepresentativeName())
                .address(req.getAddress())
                .phone(req.getPhone())
                .email(req.getEmail())
                .build());
    }

    @Transactional(readOnly = true)
    public Page<Partner> getList(Long companyId, String keyword, Partner.PartnerType type, Pageable pageable) {
        return partnerRepository.findByCompanyIdAndFilter(companyId, keyword, type, pageable);
    }

    @Transactional(readOnly = true)
    public Partner getById(Long companyId, Long id) {
        return partnerRepository.findByIdAndCompanyIdAndDeletedFalse(id, companyId)
                .orElseThrow(() -> BusinessException.notFound("거래처"));
    }

    @Transactional
    public Partner update(Long companyId, Long id, PartnerRequest req) {
        Partner partner = getById(companyId, id);
        partner.update(req.getPartnerName(), req.getBusinessNo(), req.getPartnerType(),
                req.getRepresentativeName(), req.getAddress(), req.getPhone(), req.getEmail());
        return partner;
    }

    @Transactional
    public void delete(Long companyId, Long id) {
        Partner partner = getById(companyId, id);
        partner.softDelete();
    }
}
