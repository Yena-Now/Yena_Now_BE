package com.example.yenanow.users.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.users.entity.Follow;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.FollowRepository;
import com.example.yenanow.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final FollowCountSyncService followCountSyncService;

    @Override
    @Transactional
    public void follow(String followerUuid, String followingUuid) {
        // 개별 UUID 검증
        validateUuid(followerUuid);
        validateUuid(followingUuid);

        User fromUser = getUserByUuid(followerUuid);
        User toUser = getUserByUuid(followingUuid);

        if (fromUser.equals(toUser)) {
            throw new BusinessException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }
        if (followRepository.existsByFromUserAndToUser(fromUser, toUser)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        followRepository.save(new Follow(fromUser, toUser));

        // Redis 카운트 증가
        incrementCounter("user:" + followerUuid, "following_count", 1);
        incrementCounter("user:" + followingUuid, "follower_count", 1);

        // 비동기 DB 백업
        followCountSyncService.syncFollowCountToDB(followerUuid);
        followCountSyncService.syncFollowCountToDB(followingUuid);
    }

    @Override
    @Transactional
    public void unfollow(String followerUuid, String followingUuid) {
        // 개별 UUID 검증
        validateUuid(followerUuid);
        validateUuid(followingUuid);

        User fromUser = getUserByUuid(followerUuid);
        User toUser = getUserByUuid(followingUuid);

        // 실제 삭제된 경우에만 Redis 카운트 감소
        int deletedCount = followRepository.deleteByFromUserAndToUser(fromUser, toUser);
        if (deletedCount > 0) {
            incrementCounter("user:" + followerUuid, "following_count", -1);
            incrementCounter("user:" + followingUuid, "follower_count", -1);
        }

        // 비동기 DB 백업
        followCountSyncService.syncFollowCountToDB(followerUuid);
        followCountSyncService.syncFollowCountToDB(followingUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(String followerUuid, String followingUuid) {
        validateUuid(followerUuid);
        validateUuid(followingUuid);

        User fromUser = getUserByUuid(followerUuid);
        User toUser = getUserByUuid(followingUuid);

        return followRepository.existsByFromUserAndToUser(fromUser, toUser);
    }

    /**
     * UUID 값 유효성 검증
     */
    private void validateUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * UUID로 User 조회 (없으면 예외)
     */
    private User getUserByUuid(String uuid) {
        return userRepository.findByUserUuid(uuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
    }

    /**
     * Redis 카운트 증감 (0 미만 방지)
     */
    private void incrementCounter(String key, String field, int delta) {
        Long newValue = redisTemplate.opsForHash().increment(key, field, delta);
        if (newValue != null && newValue < 0) {
            redisTemplate.opsForHash().put(key, field, "0");
        }
    }
}
