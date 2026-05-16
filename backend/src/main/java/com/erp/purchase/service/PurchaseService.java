package com.erp.purchase.service;

import com.erp.accounting.entity.JournalEntry;
import com.erp.accounting.service.JournalEntryService;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.DocSequenceService;
import com.erp.inventory.service.StockService;
import com.erp.purchase.dto.PurchaseOrderRequest;
import com.erp.purchase.dto.PurchaseReceiptRequest;
import com.erp.purchase.entity.PurchaseOrder;
import com.erp.purchase.entity.PurchaseOrderItem;
import com.erp.purchase.entity.PurchaseReceipt;
import com.erp.purchase.entity.PurchaseReceiptItem;
import com.erp.purchase.repository.PurchaseOrderRepository;
import com.erp.purchase.repository.PurchaseReceiptRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PurchaseService {

    private final PurchaseOrderRepository orderRepository;
    private final PurchaseReceiptRepository receiptRepository;
    private final DocSequenceService docSequenceService;
    private final StockService stockService;
    private final JournalEntryService journalEntryService;

    @Transactional
    public PurchaseOrder createOrder(Long companyId, PurchaseOrderRequest req, String operator) {
        String orderNo = docSequenceService.next(companyId, "PO");
        PurchaseOrder order = PurchaseOrder.builder()
                .companyId(companyId)
                .orderNo(orderNo)
                .partnerId(req.getPartnerId())
                .orderDate(req.getOrderDate())
                .expectedDate(req.getExpectedDate())
                .remark(req.getRemark())
                .build();

        req.getItems().forEach(item -> order.getItems().add(
                PurchaseOrderItem.builder()
                        .purchaseOrder(order)
                        .itemId(item.getItemId())
                        .orderQty(item.getOrderQty())
                        .unitPrice(item.getUnitPrice())
                        .build()));

        order.calculateTotal();
        return orderRepository.save(order);
    }

    @Transactional
    public PurchaseOrder confirmOrder(Long companyId, Long orderId) {
        PurchaseOrder order = getOrder(companyId, orderId);
        order.confirm();
        return order;
    }

    @Transactional
    public PurchaseReceipt createReceipt(Long companyId, PurchaseReceiptRequest req, String operator) {
        PurchaseOrder order = getOrder(companyId, req.getOrderId());
        if (order.getStatus() == PurchaseOrder.OrderStatus.DRAFT) {
            throw BusinessException.badRequest("확정된 발주서에만 입고 처리할 수 있습니다.");
        }

        String receiptNo = docSequenceService.next(companyId, "PR");
        PurchaseReceipt receipt = PurchaseReceipt.builder()
                .companyId(companyId)
                .receiptNo(receiptNo)
                .purchaseOrderId(order.getId())
                .partnerId(order.getPartnerId())
                .warehouseId(req.getWarehouseId())
                .receiptDate(req.getReceiptDate())
                .remark(req.getRemark())
                .build();

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (PurchaseReceiptRequest.ReceiptItemDto itemDto : req.getItems()) {
            receipt.getItems().add(PurchaseReceiptItem.builder()
                    .purchaseReceipt(receipt)
                    .orderItemId(itemDto.getOrderItemId())
                    .itemId(itemDto.getItemId())
                    .qty(itemDto.getQty())
                    .unitPrice(itemDto.getUnitPrice())
                    .build());

            // 재고 증가 (이동평균법)
            stockService.receiveStock(companyId, itemDto.getItemId(), req.getWarehouseId(),
                    itemDto.getQty(), itemDto.getUnitPrice(), "PURCHASE_RECEIPT", null, operator);

            totalAmount = totalAmount.add(itemDto.getQty().multiply(itemDto.getUnitPrice()));

            // 발주 아이템 수령 수량 갱신
            order.getItems().stream()
                    .filter(oi -> oi.getId().equals(itemDto.getOrderItemId()))
                    .findFirst()
                    .ifPresent(oi -> oi.addReceivedQty(itemDto.getQty()));
        }

        receipt.calculateTotal();
        PurchaseReceipt saved = receiptRepository.save(receipt);

        // 회계 자동분개
        JournalEntry journal = journalEntryService.createPurchaseJournal(
                companyId, saved.getId(), saved.getTotalAmount(), req.getReceiptDate());
        saved.linkJournalEntry(journal.getId());

        // 발주서 완료 처리
        order.complete();

        return saved;
    }

    @Transactional(readOnly = true)
    public Page<PurchaseOrder> getOrders(Long companyId, Pageable pageable) {
        return orderRepository.findByCompanyIdAndDeletedFalse(companyId, pageable);
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getOrder(Long companyId, Long id) {
        return orderRepository.findByIdAndCompanyIdAndDeletedFalse(id, companyId)
                .orElseThrow(() -> BusinessException.notFound("발주서"));
    }

    @Transactional(readOnly = true)
    public Page<PurchaseReceipt> getReceipts(Long companyId, Pageable pageable) {
        return receiptRepository.findByCompanyIdAndDeletedFalse(companyId, pageable);
    }
}
