package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.dto.query.LikeUserQueryDto;
import com.example.yenanow.gallery.entity.NcutLike;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NcutLikeRepository extends JpaRepository<NcutLike, String>,
    NcutLikeQueryRepository {

    boolean existsByNcutNcutUuidAndUserUserUuid(String ncutUuid, String userUuid);

    Optional<NcutLike> findByNcutNcutUuidAndUserUserUuid(String ncutUuid, String userUuid);

    long countByNcutNcutUuid(String ncutUuid);

    @Query(value = "SELECT " +
        "    u.user_uuid AS userUuid, " +
        "    u.name AS name, " +
        "    u.nickname AS nickname, " +
        "    u.profile_url AS profileUrl, " +
        "    u.deleted_at AS deletedAt " +
        "FROM ncut_like nl " +
        "JOIN users u ON nl.user_uuid = u.user_uuid " +
        "WHERE nl.ncut_uuid = :ncutUuid " +
        "ORDER BY nl.created_at DESC",
        countQuery = "SELECT count(*) FROM ncut_like WHERE ncut_uuid = :ncutUuid",
        nativeQuery = true)
    Page<LikeUserQueryDto> findLikesWithWithdrawnUsers(@Param("ncutUuid") String ncutUuid,
        Pageable pageable);
}
