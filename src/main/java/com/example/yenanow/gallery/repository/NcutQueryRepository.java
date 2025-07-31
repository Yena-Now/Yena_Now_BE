package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.Visibility;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NcutQueryRepository {

    Page<Ncut> findPublicGalleryWithUser(Pageable pageable);

    Page<Ncut> findFollowingsGalleryWithUser(List<String> userUuids, List<Visibility> visibilities,
        Pageable pageable);

    void updateCommentCount(String ncutUuid, int commentCount);
}
