package com.xxxx.ddd.application.service.placeorder.mq;

public interface MQPlaceOrderTokenService {
    boolean submitOrderToQueued(String tokenTickerUser, Long userId, Long ticketId, Integer quantity);
}
