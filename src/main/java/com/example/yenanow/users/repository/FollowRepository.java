package com.example.yenanow.users.repository;

import com.example.yenanow.users.entity.Follow;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, String> {

  // 특정 사용자가 팔로우하는 대상 UUID 목록
  @Query("SELECT f.toUser FROM Follow f WHERE f.fromUser = :userUuid")
  List<String> findFollowingUuids(@Param("userUuid") String userUuid);

  // 특정 팔로우 관계가 존재하는지 여부
  boolean existsByFromUserAndToUser(String fromUser, String toUser);

  // 특정 팔로우 관계 삭제
  void deleteByFromUserAndToUser(String fromUser, String toUser);
}
