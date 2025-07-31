package com.example.yenanow.users.repository;

import java.util.Optional;

public interface UserQueryRepository {

    void updateFollowCount(String userUuid, int followerCount, int followingCount);

    Optional<String> findNicknameById(String userUuid);
}