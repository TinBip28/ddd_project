package com.xxxx.ddd.infrastructure.gateway;

import com.xxxx.ddd.domain.model.entity.PaymentTransaction;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

@Service
public class VnPayGatewayServiceImpl {
    private final String SECRET_KEY = "A5804EB6E1C63A6E771729A696A841E6"; // Mã bí mật ngân hàng cấp
    private final String VNP_URL = "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html";
    public String createPaymentUrl(PaymentTransaction transaction) throws UnsupportedEncodingException {
        Map<String, String> vnp_Params = new TreeMap<>();
        vnp_Params.put("vnp_Version", "2.1.0");
        vnp_Params.put("vnp_Command", "pay");
        vnp_Params.put("vnp_TmnCode", "VNPAY");
        vnp_Params.put("vnp_Amount", transaction.getAmount().multiply(new java.math.BigDecimal(100)).toBigInteger().toString());
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_BankCode", "NCB");
        // Rút ngắn vnp_TxnRef (Ví dụ: dùng 12 ký tự cuối của UUID)
        String txnRef = transaction.getPaymentId().substring(transaction.getPaymentId().length() - 12);
        vnp_Params.put("vnp_TxnRef", txnRef);

        vnp_Params.put("vnp_OrderType", "other");
        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang: " + transaction.getOrderNumber());
        vnp_Params.put("vnp_Locale", "vn");
        vnp_Params.put("vnp_ReturnUrl", "http://127.0.0.1:8080/payment/callback");
        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
        vnp_Params.put("vnp_CreateDate", java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now()));
        // 1. Tạo chuỗi dữ liệu (Data) để Hash
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        // Duyệt qua TreeMap (đã sắp xếp A-Z)
        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
            String fieldName = entry.getKey();
            String fieldValue = entry.getValue();
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                // Build hash data & query string đồng thời
                String encodedName = URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString());
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()).replace("+", "%20");

                hashData.append(encodedName).append('=').append(encodedValue).append('&');
                query.append(encodedName).append('=').append(encodedValue).append('&');
            }
        }

        // Xóa ký tự & ở cuối
        String hashDataStr = hashData.substring(0, hashData.length() - 1);
        String queryStr = query.substring(0, query.length() - 1);
        // 2. Hash với Secret Key
        String vnp_SecureHash = hmacSHA512("SECRET", hashDataStr);
        return "https://sandbox.vnpayment.vn/paymentv2/vpcpay.html" + "?" + queryStr + "&vnp_SecureHash=" + vnp_SecureHash;
    }
//    public String createPaymentUrl(PaymentTransaction transaction) throws UnsupportedEncodingException {
//        Map<String, String> vnp_Params = new TreeMap<>(); // TreeMap tự động sắp xếp A-Z
//        vnp_Params.put("vnp_Version", "2.1.0");
//        vnp_Params.put("vnp_Command", "pay");
//        vnp_Params.put("vnp_TmnCode", "2QX1S61I");
//        vnp_Params.put("vnp_Amount", transaction.getAmount().multiply(new java.math.BigDecimal(100)).toBigInteger().toString());
//        vnp_Params.put("vnp_CurrCode", "VND");
//        vnp_Params.put("vnp_TxnRef", transaction.getPaymentId());
//        vnp_Params.put("vnp_OrderInfo", "Thanh toan don hang:" + transaction.getOrderNumber());
//        vnp_Params.put("vnp_Locale", "vn");
//        vnp_Params.put("vnp_ReturnUrl", "https://yourwebsite.com/payment/callback");
//        vnp_Params.put("vnp_IpAddr", "127.0.0.1");
//        vnp_Params.put("vnp_CreateDate", java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss").format(java.time.LocalDateTime.now()));
//        // 1. Tạo chuỗi dữ liệu (Data) từ các tham số đã sắp xếp
//        StringBuilder hashData = new StringBuilder();
//        StringBuilder query = new StringBuilder();
//        for (Map.Entry<String, String> entry : vnp_Params.entrySet()) {
//            String fieldName = entry.getKey();
//            String fieldValue = entry.getValue();
//
//            if ((fieldValue != null) && (fieldValue.length() > 0)) {
//                // Build hash data
//                hashData.append(fieldName);
//                hashData.append('=');
//                // QUAN TRỌNG: Thay thế dấu + thành %20 để khớp với VNPAY
//                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()).replace("+", "%20"));
//
//                // Build query
//                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
//                query.append('=');
//                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()).replace("+", "%20"));
//                hashData.append('&');
//                query.append('&');
//            }
//        }
//
//        // Xóa ký tự & cuối cùng
//        hashData.deleteCharAt(hashData.length() - 1);
//        query.deleteCharAt(query.length() - 1);
//        // 2. Băm dữ liệu với Secret Key (HMAC-SHA512) để tạo Signature
//        String vnp_SecureHash = hmacSHA512(SECRET_KEY, hashData.toString());
//        // 3. Trả về URL hoàn chỉnh
//        return VNP_URL + "?" + query.toString() + "&vnp_SecureHash=" + vnp_SecureHash;
//    }
    private String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] hashBytes = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi tạo Signature", e);
        }
    }
}
