package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.dto.response.NcutDetailResponse;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.Visibility;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NcutQueryRepository {

    Page<Ncut> findPublicGalleryWithUser(Pageable pageable);

    Page<Ncut> findFollowingsGalleryWithUser(List<String> userUuids, List<Visibility> visibilities,
        Pageable pageable);

    void updateCommentCount(String ncutUuid, int commentCount);

    Optional<NcutDetailResponse> findNcutById(String ncutUuid);
}
