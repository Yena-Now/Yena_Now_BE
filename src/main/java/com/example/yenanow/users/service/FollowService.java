package com.example.yenanow.users.service;

import com.example.yenanow.users.dto.response.FollowerResponse;
import com.example.yenanow.users.dto.response.FollowingResponse;

public interface FollowService {

    void follow(String followerUuid, String followingUuid);

    void unfollow(String followerUuid, String followingUuid);

    boolean isFollowing(String followerUuid, String followingUuid);

    FollowingResponse getFollowings(String targetUserUuid, String currentUserUuid, int pageNum,
        int display);

    FollowerResponse getFollowers(String userUuid, String currentUserUuid, int pageNum,
        int display);
}