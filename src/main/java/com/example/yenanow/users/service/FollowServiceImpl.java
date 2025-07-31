package com.example.yenanow.users.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.util.UuidUtil;
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
        UuidUtil.validateUuid(followerUuid);
        UuidUtil.validateUuid(followingUuid);

        User fromUser = UuidUtil.getUserByUuid(userRepository, followerUuid);
        User toUser = UuidUtil.getUserByUuid(userRepository, followingUuid);

        if (fromUser.equals(toUser)) {
            throw new BusinessException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }
        if (followRepository.existsByFromUserAndToUser(fromUser, toUser)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        followRepository.save(new Follow(fromUser, toUser));

        // Redis 카운트 증가
        UuidUtil.incrementCounter(redisTemplate, "user:" + followerUuid, "following_count", 1);
        UuidUtil.incrementCounter(redisTemplate, "user:" + followingUuid, "follower_count", 1);

        // 비동기 DB 백업
        followCountSyncService.syncFollowCountToDB(followerUuid);
        followCountSyncService.syncFollowCountToDB(followingUuid);

    }

    @Override
    @Transactional
    public void unfollow(String followerUuid, String followingUuid) {
        // 개별 UUID 검증
        UuidUtil.validateUuid(followerUuid);
        UuidUtil.validateUuid(followingUuid);

        User fromUser = UuidUtil.getUserByUuid(userRepository, followerUuid);
        User toUser = UuidUtil.getUserByUuid(userRepository, followingUuid);

        // 실제 삭제된 경우에만 Redis 카운트 감소
        int deletedCount = followRepository.deleteByFromUserAndToUser(fromUser, toUser);
        if (deletedCount > 0) {
            UuidUtil.incrementCounter(redisTemplate, "user:" + followerUuid, "following_count", -1);
            UuidUtil.incrementCounter(redisTemplate, "user:" + followingUuid, "follower_count", -1);
        }

        // 비동기 DB 백업
        followCountSyncService.syncFollowCountToDB(followerUuid);
        followCountSyncService.syncFollowCountToDB(followingUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(String followerUuid, String followingUuid) {
        // 개별 UUID 검증
        UuidUtil.validateUuid(followerUuid);
        UuidUtil.validateUuid(followingUuid);

        User fromUser = UuidUtil.getUserByUuid(userRepository, followerUuid);
        User toUser = UuidUtil.getUserByUuid(userRepository, followingUuid);

        return followRepository.existsByFromUserAndToUser(fromUser, toUser);
    }
}
