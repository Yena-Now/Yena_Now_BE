package com.example.yenanow.users.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.users.entity.Follow;
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
        validateUsersExist(followerUuid, followingUuid);

        if (followerUuid.equals(followingUuid)) {
            throw new BusinessException(ErrorCode.SELF_FOLLOW_NOT_ALLOWED);
        }
        if (followRepository.existsByFromUserAndToUser(followerUuid, followingUuid)) {
            throw new BusinessException(ErrorCode.FOLLOW_ALREADY_EXISTS);
        }

        followRepository.save(new Follow(followerUuid, followingUuid));
    }

    @Override
    @Transactional
    public void unfollow(String followerUuid, String followingUuid) {
        validateUuid(followerUuid);
        validateUuid(followingUuid);
        validateUsersExist(followerUuid, followingUuid);

        // 언팔로우는 멱등성을 보장
        followRepository.deleteByFromUserAndToUser(followerUuid, followingUuid);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFollowing(String followerUuid, String followingUuid) {
        validateUuid(followerUuid);
        validateUuid(followingUuid);
        validateUsersExist(followerUuid, followingUuid);

        return followRepository.existsByFromUserAndToUser(followerUuid, followingUuid);
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
     * 두 사용자 모두 DB에 존재하는지 검증
     */
    private void validateUsersExist(String followerUuid, String followingUuid) {
        if (!userRepository.existsByUuid(followerUuid) || !userRepository.existsByUuid(
            followingUuid)) {
            throw new BusinessException(ErrorCode.NOT_FOUND_USER);
        }
    }
}
