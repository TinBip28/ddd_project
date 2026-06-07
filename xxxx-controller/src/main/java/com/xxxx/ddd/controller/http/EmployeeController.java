package com.xxxx.ddd.controller.http;


import com.xxxx.ddd.application.service.employee.cache.EmployeeCacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/sign-in")
public class EmployeeController {

    @Autowired
    private EmployeeCacheService employeeCacheService;

    /**
     * Chấm công hôm nay cho user
     */
    @PostMapping("/{userId}")
    public String signInToday(@PathVariable("userId") String userId) {
        employeeCacheService.signIn(userId);
        return "Sign-in successful for " + userId + " at " + LocalDate.now();
    }

    /**
     * Chấm công vào ngày tùy ý cho user
     */
    @PostMapping("/{userId}/any-date")
    public String signInOnSpecificDate(
            @PathVariable("userId") String userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        employeeCacheService.signInOnSpecificDay(userId, date); // Chấm công vào ngày đã chỉ định
        return "Sign-in successful for user " + userId + " on " + date;
    }

    /**
     * Kiểm tra đã chấm công vào ngày cụ thể chưa
     */
    @GetMapping("/{userId}/check")
    public boolean hasSignedIn(
            @PathVariable("userId") String userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return employeeCacheService.hasSignedIn(userId, date);
    }

    /**
     * Thống kê tổng số ngày làm trong tháng
     */
    @GetMapping("/{userId}/monthly-count")
    public long getMonthlyCount(
            @PathVariable("userId") String userId,
            @RequestParam("month") String month
    ) {
        LocalDate date = LocalDate.parse(month + "-01");
        log.info("month | {}", date);
        return employeeCacheService.getMonthlySignCount(userId, date);
    }

    /**
     * Thống kê tổng số ngày làm trong tháng, cụ thể ngày nào?
     */
    @GetMapping("/{userId}/monthly-sign-details")
    public Map<String, Object> getMonthlySignDetails(
            @PathVariable("userId") String userId,
            @RequestParam("month") String month
    ) {
        LocalDate date = LocalDate.parse(month + "-01");
        log.info("getMonthlySignDetails | {}", date);
        return employeeCacheService.getMonthlySignDetails(userId, date);
    }

    /**
     * Lấy ngày đầu tiên có mặt trong tháng
     */
    @GetMapping("/{userId}/first-day")
    public int getFirstSignDay(
            @PathVariable("userId") String userId,
            @RequestParam("month") String month
    ) {
        LocalDate date = LocalDate.parse(month + "-01");
        log.info("month | {}", date);
        return employeeCacheService.getFirstSignDay(userId, date);
    }

    /**
     * Tính số ngày làm liên tục tính từ ngày hiện tại
     */
    @GetMapping("/{userId}/consecutive-days")
    public int getConsecutiveDays(
            @PathVariable("userId") String userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return employeeCacheService.getConsecutiveDays(userId, date);
    }

    /**
     * Tổng hợp toàn bộ thống kê trong 1 API
     */
    @GetMapping("/{userId}/summary")
    public Map<String, Object> getSummary(
            @PathVariable String userId,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return Map.of(
                "date", date,
                "hasSignedIn", employeeCacheService.hasSignedIn(userId, date),
                "monthlyCount", employeeCacheService.getMonthlySignCount(userId, date),
                "firstSignDay", employeeCacheService.getFirstSignDay(userId, date),
                "consecutiveDays", employeeCacheService.getConsecutiveDays(userId, date)
        );
    }
}
