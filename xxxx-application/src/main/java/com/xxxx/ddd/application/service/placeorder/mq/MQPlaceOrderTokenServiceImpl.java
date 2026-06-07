package com.xxxx.ddd.application.service.placeorder.mq;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MQPlaceOrderTokenServiceImpl implements MQPlaceOrderTokenService {

    private static final String PLACE_ORDER_ID_KEY = "PLACE_ORDER_ID_KEY_";

    @Override
    public boolean submitOrderToQueued(String tokenTickerUser, Long userId, Long ticketId, Integer quantity) {
        log.info("submitOrderToQueued | {} | {} | {} | {}", tokenTickerUser, userId, ticketId, quantity);

        String tokenKey = getOrderTokenKey(tokenTickerUser);
        Integer checkTokenKeyDuplicated = 1;
        return false;
    }

    private String getOrderTokenKey(String tokenTickerUser) {
        return PLACE_ORDER_ID_KEY + tokenTickerUser;
    }
}
