package com.xxxx.ddd.application.service.placeorder;

import com.xxxx.ddd.application.model.response.PlaceOrderResponse;

public interface PlaceOrderService {
    boolean startOrderByUser(Long userId, Long ticketId, Integer quantity);
}
