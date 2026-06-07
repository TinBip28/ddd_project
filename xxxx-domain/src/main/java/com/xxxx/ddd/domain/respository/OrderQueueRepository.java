package com.xxxx.ddd.domain.respository;

import com.xxxx.ddd.domain.model.entity.OrderQueue;

import java.util.Optional;

public interface OrderQueueRepository {
    OrderQueue save(OrderQueue orderQueue);
    Optional<OrderQueue> findByToken(String token);
    boolean updateStatus(String token, int status, String orderNumber, String message);
}