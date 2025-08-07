package com.example.yenanow.common.util;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import org.springframework.data.redis.core.StringRedisTemplate;

public class UuidUtil {

    /**
     * UUID 값 유효성 검증
     */
    public static void validateUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * UUID로 User 조회 (없으면 예외)
     */
    public static User getUserByUuid(UserRepository userRepository, String uuid) {
        return userRepository.findByUserUuid(uuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
    }

    /**
     * Redis 카운트 증감 (0 미만 방지)
     */
    public static Long incrementCounter(StringRedisTemplate redisTemplate, String key, String field,
        int delta) {
        Long newValue = redisTemplate.opsForHash().increment(key, field, delta);
        if (newValue != null && newValue < 0) {
            redisTemplate.opsForHash().put(key, field, "0");
            newValue = 0L;
        }

        return newValue;
    }
}
