package com.example.yenanow.users.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.users.entity.Follow;
import com.example.yenanow.users.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;

  @Override
  @Transactional
  public void follow(String followerUuid, String followingUuid) {
    if (followerUuid == null || followingUuid == null || followerUuid.isBlank()
        || followingUuid.isBlank()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
    if (followerUuid.equals(followingUuid)) {
      throw new BusinessException(ErrorCode.BAD_REQUEST); // 자기 자신 팔로우 불가
    }
    if (followRepository.existsByFromUserAndToUser(followerUuid, followingUuid)) {
      throw new BusinessException(ErrorCode.ALREADY_EXISTS); // 이미 팔로우 중
    }
    followRepository.save(new Follow(followerUuid, followingUuid));
  }

  @Override
  @Transactional
  public void unfollow(String followerUuid, String followingUuid) {
    if (followerUuid == null || followingUuid == null || followerUuid.isBlank()
        || followingUuid.isBlank()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
    if (!followRepository.existsByFromUserAndToUser(followerUuid, followingUuid)) {
      throw new BusinessException(ErrorCode.NOT_FOUND); // 팔로우 관계 없음
    }
    followRepository.deleteByFromUserAndToUser(followerUuid, followingUuid);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isFollowing(String followerUuid, String followingUuid) {
    if (followerUuid == null || followingUuid == null || followerUuid.isBlank()
        || followingUuid.isBlank()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }
    return followRepository.existsByFromUserAndToUser(followerUuid, followingUuid);
  }
}
