package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.dto.response.NcutLikesResponseItem;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NcutLikeQueryRepository {

    Optional<Page<NcutLikesResponseItem>> findNcutLikeByNcutUuid(String ncutUuid,
        Pageable pageable);
}
