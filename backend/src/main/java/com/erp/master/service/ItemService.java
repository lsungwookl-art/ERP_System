package com.erp.master.service;

import com.erp.common.exception.BusinessException;
import com.erp.common.service.DocSequenceService;
import com.erp.master.dto.ItemRequest;
import com.erp.master.entity.Item;
import com.erp.master.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final DocSequenceService docSequenceService;

    @Transactional
    public Item create(Long companyId, ItemRequest req) {
        String code = req.getItemCode() != null ? req.getItemCode()
                : docSequenceService.next(companyId, "ITEM");

        if (itemRepository.existsByItemCodeAndCompanyIdAndDeletedFalse(code, companyId)) {
            throw BusinessException.conflict("이미 사용 중인 품목 코드입니다: " + code);
        }

        return itemRepository.save(Item.builder()
                .companyId(companyId)
                .itemCode(code)
                .itemName(req.getItemName())
                .categoryId(req.getCategoryId())
                .unit(req.getUnit())
                .standardPrice(req.getStandardPrice())
                .standardCost(req.getStandardCost())
                .safetyStock(req.getSafetyStock())
                .description(req.getDescription())
                .build());
    }

    @Transactional(readOnly = true)
    public Page<Item> getList(Long companyId, String keyword, Pageable pageable) {
        return itemRepository.findByCompanyIdAndKeyword(companyId, keyword, pageable);
    }

    @Transactional(readOnly = true)
    public Item getById(Long companyId, Long id) {
        return itemRepository.findByIdAndCompanyIdAndDeletedFalse(id, companyId)
                .orElseThrow(() -> BusinessException.notFound("품목"));
    }

    @Transactional
    public Item update(Long companyId, Long id, ItemRequest req) {
        Item item = getById(companyId, id);
        item.update(req.getItemName(), req.getUnit(), req.getStandardPrice(),
                req.getStandardCost(), req.getSafetyStock(), req.getDescription(), req.getCategoryId());
        return item;
    }

    @Transactional
    public void delete(Long companyId, Long id) {
        Item item = getById(companyId, id);
        item.softDelete();
    }
}
