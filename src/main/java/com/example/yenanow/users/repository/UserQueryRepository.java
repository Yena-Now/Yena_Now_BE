package com.example.yenanow.users.repository;

public interface UserQueryRepository {

    void updateFollowCount(String userUuid, int followerCount, int followingCount);
}
