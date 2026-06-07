package com.xxxx.ddd.application.service.employee.cache;

import com.xxxx.ddd.infrastructure.distributed.redisson.RedisDistributedService;
import org.redisson.api.RBitSet;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class EmployeeCacheService {

    @Autowired
    private RedissonClient redissonClient;
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /**
     * Chấm công ngày hôm nay
     */
    public void signIn(String userId) {
        signIn(userId, LocalDate.now());
    }

    /**
     * Chấm công vào ngày cụ thể
     */
    public void signIn(String userId, LocalDate date) {

//        if (LocalTime.now().isAfter(SIGN_IN_CUT_OFF_TIME)) {
//            return "Chấm công chỉ hợp lệ trước 8h sáng!";
//        }
        String key = getKey(userId, date);
        int offset = date.getDayOfMonth() - 1; // BitMap 0-based
        RBitSet bitSet = redissonClient.getBitSet(key);
        bitSet.set(offset, true);
        // STRLEN user:sign:10001:202504 = 2 vậy đúng: 2 bytes = 16 bits. ngày 16/04 offset = 15 -> true

        // Lưu thời gian chấm công vào MySQL -> kết hợp sẽ tốt hơn nhiều trong nhiều cases
//        saveSignInTime(userId, date, signInTime);
    }

    /**
     * Chấm công vào ngày tùy chỉnh
     */
    public void signInOnSpecificDay(String userId, LocalDate date) {
        String key = getKey(userId, date);
        int offset = date.getDayOfMonth() - 1; // BitMap 0-based
        RBitSet bitSet = redissonClient.getBitSet(key);
        bitSet.set(offset, true);
    }

    /**
     * Kiểm tra hôm nay đã chấm công chưa
     */
    public boolean hasSignedIn(String userId, LocalDate date) {
        String key = getKey(userId, date);
        int offset = date.getDayOfMonth() - 1;
        return redissonClient.getBitSet(key).get(offset);
    }

    /**
     * Tổng số ngày có mặt trong tháng
     */
    public long getMonthlySignCount(String userId, LocalDate date) {
        return redissonClient.getBitSet(getKey(userId, date)).cardinality();
    }
    /**
     * Tổng số ngày có mặt trong tháng, cụ thể là ngày nào?
     */
    public Map<String, Object> getMonthlySignDetails(String userId, LocalDate date) {
        RBitSet bitSet = redissonClient.getBitSet(getKey(userId, date));
        int totalDays = date.lengthOfMonth();  // Tổng số ngày trong tháng
        List<Integer> signDays = new ArrayList<>();  // Danh sách các ngày đi làm

        // Kiểm tra tất cả các ngày trong tháng
        for (int i = 0; i < totalDays; i++) {
            if (bitSet.get(i)) {
                // Nếu bitSet[i] == true (tức là có làm việc vào ngày đó), thêm vào danh sách
                signDays.add(i + 1);  // +1 vì ngày trong tháng bắt đầu từ 1, còn BitSet bắt đầu từ 0
            }
        }

        // Trả về kết quả dưới dạng Map: tổng số ngày và danh sách các ngày đi làm
        Map<String, Object> result = new HashMap<>();
        result.put("totalSignCount", signDays.size());
        result.put("signDays", signDays);

        return result;
    }


    /**
     * Ngày đầu tiên có mặt trong tháng
     */
    public int getFirstSignDay(String userId, LocalDate date) {
        RBitSet bitSet = redissonClient.getBitSet(getKey(userId, date));
        for (int i = 0; i < 31; i++) {
            if (bitSet.get(i)) return i + 1;
        }
        return -1;
    }

    /**
     * Số ngày đi làm liên tục tính từ ngày hiện tại
     */
    public int getConsecutiveDays(String userId, LocalDate date) {
        RBitSet bitSet = redissonClient.getBitSet(getKey(userId, date));
        int day = date.getDayOfMonth() - 1;
        int count = 0;

        for (int i = day; i >= 0; i--) {
            if (bitSet.get(i)) count++;
            else break;
        }

        return count;
    }

    /**
     * Tạo key lưu BitMap cho từng user theo tháng
     */
    private String getKey(String userId, LocalDate date) {
        return "user:sign:" + userId + ":" + date.format(MONTH_FORMATTER);
    }
}
