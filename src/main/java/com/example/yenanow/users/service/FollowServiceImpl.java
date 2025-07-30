package com.example.yenanow.users.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.users.entity.Follow;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.FollowRepository;
import com.example.yenanow.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public void follow(String followerUuid, String followingUuid) {
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
    }

    @Override
    @Transactional
    public void unfollow(String followerUuid, String followingUuid) {
        validateUuid(followerUuid);
        validateUuid(followingUuid);

        User fromUser = getUserByUuid(followerUuid);
        User toUser = getUserByUuid(followingUuid);

        // 언팔로우는 멱등성 보장
        followRepository.deleteByFromUserAndToUser(fromUser, toUser);
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
        return userRepository.findByUuid(uuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
    }
}
