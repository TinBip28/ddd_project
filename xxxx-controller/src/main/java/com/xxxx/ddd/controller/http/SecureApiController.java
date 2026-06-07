package com.xxxx.ddd.controller.http;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/secure") // Các API trong đây sẽ được filter bảo vệ
public class SecureApiController {

    @PostMapping("/data")
    public ResponseEntity<Map<String, Object>> postSecureData(@RequestBody Map<String, Object> payload) {
        // Nếu request đến được đây, chữ ký đã hợp lệ
        return ResponseEntity.ok(Map.of("status", "success", "message", "Secure data processed!", "receivedPayload", payload));
    }

    @GetMapping("/info")
    public ResponseEntity<Map<String, String>> getSecureInfo() {
        return ResponseEntity.ok(Map.of("status", "success", "message", "This is secure information."));
    }
}