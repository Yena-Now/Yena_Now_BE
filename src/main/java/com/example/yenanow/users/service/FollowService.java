package com.example.yenanow.users.service;

public interface FollowService {

  void follow(String followerUuid, String followingUuid);

  void unfollow(String followerUuid, String followingUuid);

  boolean isFollowing(String followerUuid, String followingUuid);
}
