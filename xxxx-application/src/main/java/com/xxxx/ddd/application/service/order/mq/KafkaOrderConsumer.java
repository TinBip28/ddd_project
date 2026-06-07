package com.xxxx.ddd.application.service.order.mq;

import com.xxxx.ddd.application.service.order.cache.StockOrderCacheService;
import com.xxxx.ddd.domain.model.entity.OrderQueue;
import com.xxxx.ddd.domain.model.entity.TickerOrder;
import com.xxxx.ddd.domain.respository.OrderQueueRepository;
import com.xxxx.ddd.domain.service.OrderDeductionDomainService;
import com.xxxx.ddd.domain.service.TickerOrderDomainService;
import com.xxxx.ddd.infrastructure.mq.PlaceOrderMQMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class KafkaOrderConsumer {

    @Autowired
    private TickerOrderDomainService tickerOrderDomainService;

    @Autowired
    private OrderDeductionDomainService orderDeductionDomainService;

    @Autowired
    private StockOrderCacheService stockOrderCacheService;

    @Autowired
    private OrderQueueRepository orderQueueRepository;

    @KafkaListener(
            topics = "order-place-topic",
            groupId = "order-consumer-group",
            concurrency = "3"
    )
    @Transactional(rollbackFor = Exception.class)
    public void processOrder(PlaceOrderMQMessage message) {
        String token = message.getToken();
        Long ticketId = message.getTicketId();
        int quantity = message.getQuantity();

        log.info("KafkaOrderConsumer: processing token={} ticketId={} qty={}", token, ticketId, quantity);

        boolean stockDecreased = false;
        try {
            boolean dbDecreased = tickerOrderDomainService.decreaseStockLevel1(ticketId, quantity);
            if (!dbDecreased) {
                log.warn("KafkaOrderConsumer: DB stock insufficient for token={}", token);
                stockOrderCacheService.increaseStockCache(ticketId, quantity);
                orderQueueRepository.updateStatus(token, 2, null, "Hết vé");
                return;
            }
            stockDecreased = true;

            String orderNumber = "MQ-" + message.getUserId() + "-" + System.currentTimeMillis();
            String nTable = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));

            TickerOrder order = new TickerOrder();
            order.setTicketId(ticketId.intValue());
            order.setQuantity(quantity);
            order.setOrderStatus(0);
            order.setUserId(message.getUserId());
            order.setOrderNumber(orderNumber);
            order.setTotalAmount(new BigDecimal(message.getUnitPrice() * quantity));
            order.setTerminalId("MQ-SGN");
            order.setOrderNotes("MQ Order -> Pending");
            orderDeductionDomainService.insertOrder(nTable, order);

            orderQueueRepository.updateStatus(token, 1, orderNumber, null);
            log.info("KafkaOrderConsumer: success token={} orderNumber={}", token, orderNumber);

        } catch (Exception e) {
            log.error("KafkaOrderConsumer: error processing token={}", token, e);
            if (stockDecreased) {
                stockOrderCacheService.increaseStockCache(ticketId, quantity);
            }
            orderQueueRepository.updateStatus(token, 2, null, "Lỗi hệ thống: " + e.getMessage());
        }
    }
}