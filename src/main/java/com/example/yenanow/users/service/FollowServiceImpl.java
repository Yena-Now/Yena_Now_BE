package com.example.yenanow.users.service;

import com.example.yenanow.users.entity.Follow;
import com.example.yenanow.users.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class FollowServiceImpl implements FollowService {

  private final FollowRepository followRepository;

  @Override
  public void follow(String followerUuid, String followingUuid) {
    // 이미 팔로우 관계가 있는지 확인
    if (followRepository.existsByFromUserAndToUser(followerUuid, followingUuid)) {
      return;
    }

    Follow follow = Follow.builder()
        .fromUser(followerUuid)
        .toUser(followingUuid)
        .build();

    followRepository.save(follow);
  }

  @Override
  public void unfollow(String followerUuid, String followingUuid) {
    followRepository.deleteByFromUserAndToUser(followerUuid, followingUuid);
  }

  @Override
  public boolean isFollowing(String followerUuid, String followingUuid) {
    return followRepository.existsByFromUserAndToUser(followerUuid, followingUuid);
  }
}
