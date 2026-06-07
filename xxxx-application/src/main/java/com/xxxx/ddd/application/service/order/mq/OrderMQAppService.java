package com.xxxx.ddd.application.service.order.mq;

import com.xxxx.ddd.domain.model.entity.OrderQueue;

public interface OrderMQAppService {

    /**
     * Tiếp nhận đơn hàng qua Kafka (async), trả về token ngay lập tức.
     * @return OrderQueue với status=PENDING
     */
    OrderQueue placeOrderMQ(Long ticketId, int quantity);

    /**
     * Kiểm tra trạng thái xử lý đơn hàng theo token.
     */
    OrderQueue getOrderStatus(String token);
}