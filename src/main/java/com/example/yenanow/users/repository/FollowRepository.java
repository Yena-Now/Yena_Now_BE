package com.example.yenanow.users.repository;

import com.example.yenanow.users.entity.Follow;
import com.example.yenanow.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, String> {

    // 특정 팔로우 관계가 존재하는지 여부 확인
    boolean existsByFromUserAndToUser(User fromUser, User toUser);

    // 특정 팔로우 관계 삭제
    // @return 삭제된 행 수
    int deleteByFromUserAndToUser(User fromUser, User toUser);

    @Modifying
    @Transactional
    @Query("UPDATE User u SET u.followerCount = :followerCount, u.followingCount = :followingCount WHERE u.userUuid = :userUuid")
    void updateFollowCount(String userUuid, int followerCount, int followingCount);
}
