package com.xxxx.ddd.application.service.placeorder.mq;

import com.xxxx.ddd.application.service.placeorder.PlaceOrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MQPlaceOrderServiceImpl implements PlaceOrderService {

    @Autowired
    private MQPlaceOrderTokenServiceImpl mqPlaceOrderTokenService;
    // Method: placeOrder by user
    @Override
    public boolean startOrderByUser(Long userId, Long ticketId, Integer quantity) {
        log.info("startOrderByUser | {} | {} | {}", userId, ticketId, quantity);
        // check ticketId available
        // getTicketItemAvailable(ticketId)

        // create ticketForUser
        String tokenTicketUser = generateTokenTicketUser(userId, ticketId); // this token saved to table Task() -> Tracking TaskByToken
        return mqPlaceOrderTokenService.submitOrderToQueued(tokenTicketUser, userId, ticketId, quantity);
    }

    private String generateTokenTicketUser(Long userId, Long ticketId) {
        return "TOKEN_TICKET_USER_" + userId + "_" + ticketId;
    }
}
