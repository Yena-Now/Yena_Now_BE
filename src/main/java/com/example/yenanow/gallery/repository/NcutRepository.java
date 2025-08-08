package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.Visibility;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface NcutRepository extends JpaRepository<Ncut, String>, NcutQueryRepository {

    // 내 갤러리 (공개 여부 무관)
    Page<Ncut> findByUserUserUuid(String userUuid, Pageable pageable);

    // 타인 갤러리
    Page<Ncut> findByUserUserUuidAndVisibility(String userUuid, Visibility visibility,
        Pageable pageable);

    // 공개 갤러리
    Page<Ncut> findByVisibility(Visibility visibility, Pageable pageable);

    // 친구 갤러리 - 기존 메서드 (남겨둠, 필요시 사용)
    Page<Ncut> findByUser_UserUuidInAndVisibilityIn(List<String> userUuids,
        List<Visibility> visibilities,
        Pageable pageable);

    // 랭킹 상세 조회용 – IN 절
    List<Ncut> findByNcutUuidIn(List<String> ncutUuid);
}
