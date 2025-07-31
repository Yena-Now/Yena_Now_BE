package com.example.yenanow.users.service;

import com.example.yenanow.users.repository.UserQueryRepository;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EnableAsync // 비동기 처리 활성화
public class FollowCountSyncService {

    private final StringRedisTemplate redisTemplate;
    private final UserQueryRepository userCustomRepository;

    @Async
    public void syncFollowCountToDB(String userUuid) {
        String key = "user:" + userUuid;
        Map<Object, Object> counts = redisTemplate.opsForHash().entries(key);

        int followerCount = Integer.parseInt(counts.getOrDefault("follower_count", "0").toString());
        int followingCount = Integer.parseInt(
            counts.getOrDefault("following_count", "0").toString());

        userCustomRepository.updateFollowCount(userUuid, followerCount, followingCount);
    }
}
