package com.example.yenanow.gallery.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.gallery.dto.response.MyGalleryResponse;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.Visibility;
import com.example.yenanow.gallery.repository.NcutRepository;
import com.example.yenanow.users.repository.FollowQueryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GalleryServiceImpl implements GalleryService {

    private final NcutRepository ncutRepository;
    private final FollowQueryRepository followQueryRepository;

    @Override
    public MyGalleryResponse getMyGallery(String userUuid, int pageNum, int display) {
        validateUserUuid(userUuid);

        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Ncut> ncutPage = ncutRepository.findByUserUuid(userUuid, pageable);

        // 데이터가 없으면 빈 응답 반환
        if (ncutPage.isEmpty()) {
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }
        return MyGalleryResponse.fromEntity(ncutPage);
    }

    @Override
    public MyGalleryResponse getOtherGallery(String userUuid, int pageNum, int display) {
        validateUserUuid(userUuid);

        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Ncut> ncutPage = ncutRepository.findByUserUuidAndVisibility(
            userUuid, Visibility.PUBLIC, pageable
        );

        if (ncutPage.isEmpty()) {
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }
        return MyGalleryResponse.fromEntity(ncutPage);
    }

    @Override
    public MyGalleryResponse getPublicGallery(int pageNum, int display) {
        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Ncut> ncutPage = ncutRepository.findPublicGalleryWithUser(pageable);

        if (ncutPage.isEmpty()) {
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }
        return MyGalleryResponse.fromEntityWithUser(ncutPage);
    }

    @Override
    public MyGalleryResponse getFollowingsGallery(String userUuid, int pageNum, int display) {
        validateUserUuid(userUuid);

        List<String> followingUuids = followQueryRepository.findFollowingUuids(userUuid);
        if (followingUuids.isEmpty()) {
            // 팔로잉한 유저가 아예 없는 경우도 정상 처리 (빈 결과 반환)
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }

        List<Visibility> allowedVisibilities = List.of(Visibility.PUBLIC, Visibility.FOLLOW);
        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Ncut> ncutPage = ncutRepository.findFollowingsGalleryWithUser(
            followingUuids, allowedVisibilities, pageable
        );

        if (ncutPage.isEmpty()) {
            return MyGalleryResponse.builder()
                .totalPages(0)
                .ncuts(List.of())
                .build();
        }
        return MyGalleryResponse.fromEntityWithUser(ncutPage);
    }

    private void validateUserUuid(String userUuid) {
        if (userUuid == null || userUuid.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
