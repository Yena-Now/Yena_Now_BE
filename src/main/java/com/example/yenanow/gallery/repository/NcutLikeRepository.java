package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.NcutLike;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NcutLikeRepository extends JpaRepository<NcutLike, String>,
    NcutLikeQueryRepository {

    boolean existsByNcutNcutUuidAndUserUserUuid(String ncutUuid, String userUuid);

    Optional<NcutLike> findByNcutNcutUuidAndUserUserUuid(String ncutUuid, String userUuid);
}
