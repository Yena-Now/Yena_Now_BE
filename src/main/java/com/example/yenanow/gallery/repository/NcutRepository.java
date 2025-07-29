package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.Visibility;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NcutRepository extends JpaRepository<Ncut, String> {

  // 내 갤러리 (공개 여부 무관)
  Page<Ncut> findByUserUuid(String userUuid, Pageable pageable);

  // 타인 갤러리 조회 (Ex - 팔로우 안한 사용자의 갤러리 조회르 하면 visibility = PUBLIC인 N컷 목록이 조회 됨)
  Page<Ncut> findByUserUuidAndVisibility(String userUuid, Visibility visibility, Pageable pageable);

  // 공개 갤러리(모든 사용자) 목록 조회
  Page<Ncut> findByVisibility(Visibility visibility, Pageable pageable);

  // 친구 갤러리 - 팔로우한 사용자들의 N컷 목록
  Page<Ncut> findByUserUuidInAndVisibilityIn(
      List<String> userUuids,
      List<Visibility> visibilities,
      Pageable pageable
  );

}
