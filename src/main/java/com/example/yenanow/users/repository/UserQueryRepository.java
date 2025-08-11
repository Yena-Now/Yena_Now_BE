package com.example.yenanow.users.repository;

import com.example.yenanow.users.dto.response.UserInviteSearchResponseItem;
import com.example.yenanow.users.dto.response.UserSearchResponseItem;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserQueryRepository {

    void updateFollowCount(String userUuid, int followerCount, int followingCount);

    Optional<String> findNicknameById(String userUuid);

    Page<UserSearchResponseItem> findUsersByKeyword(String currentUserUuid, String keyword,
        Pageable pageable);

    Page<UserInviteSearchResponseItem> findFollowersByKeyword(String currentUserUuid,
        String keyword, Pageable pageable);
}