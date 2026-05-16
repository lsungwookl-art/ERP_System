package com.erp.bom.service;

import com.erp.bom.dto.BomLineDto;
import com.erp.bom.dto.BomRequest;
import com.erp.bom.entity.Bom;
import com.erp.bom.repository.BomRepository;
import com.erp.common.exception.BusinessException;
import com.erp.master.entity.Item;
import org.springframework.http.HttpStatus;
import com.erp.master.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BomService {

    private final BomRepository bomRepository;
    private final ItemRepository itemRepository;

    @Transactional(readOnly = true)
    public List<BomLineDto> getBom(Long companyId, Long parentItemId) {
        List<Bom> boms = bomRepository
                .findByCompanyIdAndParentItemIdAndDeletedFalseOrderByChildItemId(companyId, parentItemId);

        List<Long> childIds = boms.stream().map(Bom::getChildItemId).toList();
        Map<Long, Item> itemMap = itemRepository.findAllById(childIds).stream()
                .collect(Collectors.toMap(Item::getId, i -> i));

        return boms.stream().map(b -> {
            Item child = itemMap.get(b.getChildItemId());
            return new BomLineDto(
                    b.getId(),
                    b.getChildItemId(),
                    child != null ? child.getItemCode() : "",
                    child != null ? child.getItemName() : "",
                    b.getQty(),
                    b.getUnit(),
                    b.getRemark()
            );
        }).collect(Collectors.toList());
    }

    @Transactional
    public Bom create(Long companyId, BomRequest req) {
        if (req.getParentItemId().equals(req.getChildItemId())) {
            throw new BusinessException("상위품목과 하위품목은 같을 수 없습니다.", "BOM_SELF_REF", HttpStatus.BAD_REQUEST);
        }
        if (bomRepository.existsByCompanyIdAndParentItemIdAndChildItemIdAndDeletedFalse(
                companyId, req.getParentItemId(), req.getChildItemId())) {
            throw new BusinessException("이미 등록된 BOM 구성입니다.", "BOM_DUPLICATE", HttpStatus.BAD_REQUEST);
        }
        return bomRepository.save(Bom.builder()
                .companyId(companyId)
                .parentItemId(req.getParentItemId())
                .childItemId(req.getChildItemId())
                .qty(req.getQty())
                .unit(req.getUnit())
                .remark(req.getRemark())
                .build());
    }

    @Transactional
    public void delete(Long companyId, Long bomId) {
        Bom bom = bomRepository.findByIdAndCompanyIdAndDeletedFalse(bomId, companyId)
                .orElseThrow(() -> BusinessException.notFound("BOM"));
        bom.softDelete();
    }
}
