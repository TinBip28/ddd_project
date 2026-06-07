package com.xxxx.ddd.application.service.order.mq;

import com.xxxx.ddd.application.service.order.cache.StockOrderCacheService;
import com.xxxx.ddd.domain.model.entity.OrderQueue;
import com.xxxx.ddd.domain.respository.OrderQueueRepository;
import com.xxxx.ddd.infrastructure.mq.KafkaOrderProducer;
import com.xxxx.ddd.infrastructure.mq.PlaceOrderMQMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

@Service
@Slf4j
public class OrderMQAppServiceImpl implements OrderMQAppService {

    @Autowired
    private StockOrderCacheService stockOrderCacheService;

    @Autowired
    private OrderQueueRepository orderQueueRepository;

    @Autowired
    private KafkaOrderProducer kafkaOrderProducer;

    @Override
    public OrderQueue placeOrderMQ(Long ticketId, int quantity) {
        // 1. Redis LUA: fast gate — same check as CAS
        int redisResult = stockOrderCacheService.decreaseStockCacheByLUA(ticketId, quantity);
        if (redisResult == -1) {
            log.info("placeOrderMQ: cache miss for ticketId={}, warming up...", ticketId);
            boolean warmed = stockOrderCacheService.addStockAvailableToCache(ticketId);
            if (!warmed) {
                return failedQueue("TICKET_NOT_FOUND", "Không tìm thấy sự kiện");
            }
            redisResult = stockOrderCacheService.decreaseStockCacheByLUA(ticketId, quantity);
        }
        if (redisResult == 0) {
            log.info("placeOrderMQ: Redis OOS for ticketId={}", ticketId);
            return failedQueue("OUT_OF_STOCK", "Hết vé");
        }

        long unitPrice = stockOrderCacheService.getEffectivePrice(ticketId);
        if (unitPrice <= 0) {
            stockOrderCacheService.increaseStockCache(ticketId, quantity);
            return failedQueue("PRICE_NOT_FOUND", "Không thể xác định giá vé");
        }

        // 2. Persist request to order_queue immediately
        int userId = ThreadLocalRandom.current().nextInt(1, 10);
        String token = "MQ-" + UUID.randomUUID().toString().replace("-", "").substring(0, 16);

        OrderQueue queue = new OrderQueue()
                .setToken(token)
                .setTicketId(ticketId.intValue())
                .setQuantity(quantity)
                .setUserId(userId)
                .setStatus(0)
                .setCreatedAt(LocalDateTime.now())
                .setUpdatedAt(LocalDateTime.now());
        orderQueueRepository.save(queue);

        // 3. Publish to Kafka — async processing
        PlaceOrderMQMessage message = new PlaceOrderMQMessage(token, ticketId, quantity, userId, unitPrice, System.currentTimeMillis());
        kafkaOrderProducer.sendOrderMessage(message);

        log.info("placeOrderMQ: queued token={} ticketId={}", token, ticketId);
        return queue;
    }

    @Override
    public OrderQueue getOrderStatus(String token) {
        return orderQueueRepository.findByToken(token)
                .orElse(null);
    }

    private OrderQueue failedQueue(String code, String msg) {
        return new OrderQueue()
                .setStatus(2)
                .setMessage(code + ": " + msg);
    }
}