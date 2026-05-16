package com.erp.sales.service;

import com.erp.accounting.entity.JournalEntry;
import com.erp.accounting.service.JournalEntryService;
import com.erp.common.exception.BusinessException;
import com.erp.common.service.DocSequenceService;
import com.erp.inventory.service.StockService;
import com.erp.sales.dto.SalesOrderRequest;
import com.erp.sales.dto.SalesShipmentRequest;
import com.erp.sales.entity.SalesOrder;
import com.erp.sales.entity.SalesOrderItem;
import com.erp.sales.entity.SalesShipment;
import com.erp.sales.entity.SalesShipmentItem;
import com.erp.sales.repository.SalesOrderRepository;
import com.erp.sales.repository.SalesShipmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class SalesService {

    private final SalesOrderRepository orderRepository;
    private final SalesShipmentRepository shipmentRepository;
    private final DocSequenceService docSequenceService;
    private final StockService stockService;
    private final JournalEntryService journalEntryService;

    @Transactional
    public SalesOrder createOrder(Long companyId, SalesOrderRequest req, String operator) {
        String orderNo = docSequenceService.next(companyId, "SO");
        SalesOrder order = SalesOrder.builder()
                .companyId(companyId)
                .orderNo(orderNo)
                .partnerId(req.getPartnerId())
                .orderDate(req.getOrderDate())
                .expectedDate(req.getExpectedDate())
                .remark(req.getRemark())
                .build();

        req.getItems().forEach(item -> order.getItems().add(
                SalesOrderItem.builder()
                        .salesOrder(order)
                        .itemId(item.getItemId())
                        .orderQty(item.getOrderQty())
                        .unitPrice(item.getUnitPrice())
                        .build()));

        order.calculateTotal();
        return orderRepository.save(order);
    }

    @Transactional
    public SalesOrder confirmOrder(Long companyId, Long orderId) {
        SalesOrder order = getOrder(companyId, orderId);
        order.confirm();
        return order;
    }

    @Transactional
    public SalesShipment createShipment(Long companyId, SalesShipmentRequest req, String operator) {
        SalesOrder order = getOrder(companyId, req.getOrderId());
        if (order.getStatus() == SalesOrder.OrderStatus.DRAFT) {
            throw BusinessException.badRequest("확정된 수주에만 출고 처리할 수 있습니다.");
        }

        String shipmentNo = docSequenceService.next(companyId, "SS");
        SalesShipment shipment = SalesShipment.builder()
                .companyId(companyId)
                .shipmentNo(shipmentNo)
                .salesOrderId(order.getId())
                .partnerId(order.getPartnerId())
                .warehouseId(req.getWarehouseId())
                .shipmentDate(req.getShipmentDate())
                .remark(req.getRemark())
                .build();

        BigDecimal totalSalesAmount = BigDecimal.ZERO;
        BigDecimal totalCostAmount = BigDecimal.ZERO;

        for (SalesShipmentRequest.ShipmentItemDto itemDto : req.getItems()) {
            // 재고 차감 → 이동평균단가 반환
            BigDecimal avgCost = stockService.issueStock(companyId, itemDto.getItemId(),
                    req.getWarehouseId(), itemDto.getQty(), "SALES_SHIPMENT", null, operator);

            SalesShipmentItem shipmentItem = SalesShipmentItem.builder()
                    .salesShipment(shipment)
                    .orderItemId(itemDto.getOrderItemId())
                    .itemId(itemDto.getItemId())
                    .qty(itemDto.getQty())
                    .unitPrice(itemDto.getUnitPrice())
                    .build();
            shipmentItem.setAvgCost(avgCost);
            shipment.getItems().add(shipmentItem);

            BigDecimal salesLine = itemDto.getQty().multiply(itemDto.getUnitPrice());
            BigDecimal costLine = itemDto.getQty().multiply(avgCost);
            totalSalesAmount = totalSalesAmount.add(salesLine);
            totalCostAmount = totalCostAmount.add(costLine);

            // 수주 아이템 출고 수량 갱신
            order.getItems().stream()
                    .filter(oi -> oi.getId().equals(itemDto.getOrderItemId()))
                    .findFirst()
                    .ifPresent(oi -> oi.addShippedQty(itemDto.getQty()));
        }

        shipment.calculateTotal();
        SalesShipment saved = shipmentRepository.save(shipment);

        // 회계 자동분개 (4줄)
        JournalEntry journal = journalEntryService.createSalesJournal(
                companyId, saved.getId(), totalSalesAmount, totalCostAmount, req.getShipmentDate());
        saved.linkJournalEntry(journal.getId());

        order.complete();
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<SalesOrder> getOrders(Long companyId, Pageable pageable) {
        return orderRepository.findByCompanyIdAndDeletedFalse(companyId, pageable);
    }

    @Transactional(readOnly = true)
    public SalesOrder getOrder(Long companyId, Long id) {
        return orderRepository.findByIdAndCompanyIdAndDeletedFalse(id, companyId)
                .orElseThrow(() -> BusinessException.notFound("수주"));
    }

    @Transactional(readOnly = true)
    public Page<SalesShipment> getShipments(Long companyId, Pageable pageable) {
        return shipmentRepository.findByCompanyIdAndDeletedFalse(companyId, pageable);
    }
}
