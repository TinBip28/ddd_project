package com.xxxx.ddd.application.service.payment.impl;

import com.xxxx.ddd.application.model.TicketOrderDTO;
import com.xxxx.ddd.application.service.order.TicketOrderAppService;
import com.xxxx.ddd.application.service.payment.PaymentAppService;
import com.xxxx.ddd.domain.model.entity.PaymentTransaction;
import com.xxxx.ddd.domain.service.PaymentDomainService;
import com.xxxx.ddd.infrastructure.distributed.redisson.RedisDistributedLocker;
import com.xxxx.ddd.infrastructure.distributed.redisson.RedisDistributedService;
import com.xxxx.ddd.infrastructure.gateway.VnPayGatewayServiceImpl;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PaymentAppServiceImpl implements PaymentAppService {
    @Autowired
    private TicketOrderAppService ticketOrderAppService;
    @Autowired
    private PaymentDomainService paymentDomainService;
    @Autowired
    private RedisDistributedService redisDistributedService;
    @Autowired
    private VnPayGatewayServiceImpl vnPayGatewayServiceImpl;

    @Override
    @Transactional()
    public String paymentOrder(Long userId, String orderNumber, String method) throws UnsupportedEncodingException {
        log.info("paymentOrder | {} | {} | {}", userId, orderNumber, method);
        // Demo: idempotent
        String yearMonth = orderNumber.substring(orderNumber.lastIndexOf("-") - 6, orderNumber.lastIndexOf("-")); // Logic lấy yearMonth
        TicketOrderDTO order = ticketOrderAppService.findByOrderNumber(yearMonth, orderNumber);
        if (order == null || order.getOrderStatus() != 0) {
            throw new RuntimeException("Đơn hàng không hợp lệ để thanh toán");
        }
        // 3. Khởi tạo Giao dịch Thanh toán
        PaymentTransaction transaction = new PaymentTransaction();
        transaction.setPaymentId(UUID.randomUUID().toString());
        transaction.setOrderNumber(orderNumber);
        transaction.setUserId(userId.intValue());
        transaction.setAmount(order.getTotalAmount());
        transaction.setPaymentMethod(method);
        paymentDomainService.createTransaction(transaction);
//        // 4. callGateway để lấy link
//        String mockRedirectUrl = "https://gateway.vnpay.vn/pay?token=" + transaction.getPaymentId();
        // 4. Lấy Link thanh toán chuẩn có Signature từ Gateway Service
        String realPaymentUrl = vnPayGatewayServiceImpl.createPaymentUrl(transaction);

        // 5. Cập nhật URL thanh toán vào DB
        paymentDomainService.updateTransactionInProgress(transaction.getPaymentId(), realPaymentUrl);
        return realPaymentUrl;

//        // 1. Dùng Redis Lock theo OrderNumber để tránh User click thanh toán 2 lần cùng lúc
//        RedisDistributedLocker lock = redisDistributedService.getDistributedLock("LOCK:PAYMENT:" + orderNumber);
//        try {
//            if (!lock.tryLock(1, 5, TimeUnit.SECONDS)) {
//                throw new RuntimeException("Giao dịch đang được xử lý, vui lòng đợi");
//            }
//            // 2. Kiểm tra đơn hàng (Sử dụng yearMonth từ mã đơn hàng giống cancelOrder)
//            String yearMonth = orderNumber.substring(orderNumber.lastIndexOf("-") - 6, orderNumber.lastIndexOf("-")); // Logic lấy yearMonth
//            TicketOrderDTO order = ticketOrderAppService.findByOrderNumber(yearMonth, orderNumber);
//
//            if (order == null || order.getOrderStatus() != 0) {
//                throw new RuntimeException("Đơn hàng không hợp lệ để thanh toán");
//            }
//            // 3. Khởi tạo Giao dịch Thanh toán
//            PaymentTransaction transaction = new PaymentTransaction();
//            transaction.setPaymentId(UUID.randomUUID().toString());
//            transaction.setOrderNumber(orderNumber);
//            transaction.setUserId(userId.intValue());
//            transaction.setAmount(order.getTotalAmount());
//            transaction.setPaymentMethod(method);
//            paymentDomainService.createTransaction(transaction);
//            // 4. Giả lập gọi Gateway để lấy link (Trong thực tế sẽ gọi Bank API tại đây)
//            String mockRedirectUrl = "https://gateway.vnpay.vn/pay?token=" + transaction.getPaymentId();
//
//            // 5. Cập nhật URL thanh toán vào DB
//            paymentDomainService.updateTransactionInProgress(transaction.getPaymentId(), mockRedirectUrl);
//            return mockRedirectUrl;
//        } catch (Exception e) {
//            log.error("Payment Order Error: ", e);
//            throw new RuntimeException(e.getMessage());
//        } finally {
//            lock.unlock();
//        }
    }
}
