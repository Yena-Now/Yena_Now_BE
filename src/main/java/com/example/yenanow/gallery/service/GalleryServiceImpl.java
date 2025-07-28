package com.example.yenanow.gallery.service;

import com.example.yenanow.gallery.dto.response.MyGalleryResponse;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.Visibility;
import com.example.yenanow.gallery.repository.NcutRepository;
import com.example.yenanow.users.repository.FollowRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GalleryServiceImpl implements GalleryService {

  private final NcutRepository ncutRepository;
  private final FollowRepository followRepository;

  @Override
  public MyGalleryResponse getMyGallery(String userUuid, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Ncut> ncutPage = ncutRepository.findByUserUuid(userUuid, pageable);
    return MyGalleryResponse.from(ncutPage);
  }

  @Override
  public MyGalleryResponse getOtherGallery(String userUuid, int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Ncut> ncutPage = ncutRepository.findByUserUuidAndVisibility(userUuid, Visibility.PUBLIC,
        pageable);
    return MyGalleryResponse.from(ncutPage);
  }

  @Override
  public MyGalleryResponse getPublicGallery(int page, int size) {
    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    Page<Ncut> ncutPage = ncutRepository.findByVisibility(Visibility.PUBLIC, pageable);
    return MyGalleryResponse.fromWithUser(ncutPage);
  }

  @Override
  public MyGalleryResponse getFollowingsGallery(String userUuid, int page, int size) {
    // 팔로잉한 유저 UUID 목록 조회
    List<String> followingUuids = followRepository.findFollowingUuids(userUuid);

    if (followingUuids.isEmpty()) {
      return MyGalleryResponse.builder()
          .totalPages(0)
          .ncuts(List.of())
          .build();
    }

    Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

    // PUBLIC + FOLLOW만 허용
    List<Visibility> allowedVisibilities = List.of(Visibility.PUBLIC, Visibility.FOLLOW);

    Page<Ncut> ncutPage = ncutRepository.findByUserUuidInAndVisibilityIn(followingUuids,
        allowedVisibilities, pageable);

    return MyGalleryResponse.fromWithUser(ncutPage);
  }
}
