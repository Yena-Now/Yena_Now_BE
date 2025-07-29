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
    if (userUuid == null || userUuid.isBlank()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }

    Pageable pageable = PageRequest.of(pageNum, display);
    Page<Ncut> ncutPage = ncutRepository.findByUserUuid(userUuid, pageable);
    if (ncutPage.isEmpty()) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    return MyGalleryResponse.from(ncutPage);
  }

  @Override
  public MyGalleryResponse getOtherGallery(String userUuid, int pageNum, int display) {
    if (userUuid == null || userUuid.isBlank()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }

    Pageable pageable = PageRequest.of(pageNum, display);
    Page<Ncut> ncutPage = ncutRepository.findByUserUuidAndVisibility(userUuid, Visibility.PUBLIC,
        pageable);
    if (ncutPage.isEmpty()) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    return MyGalleryResponse.from(ncutPage);
  }

  @Override
  public MyGalleryResponse getPublicGallery(int pageNum, int display) {
    Pageable pageable = PageRequest.of(pageNum, display);
    Page<Ncut> ncutPage = ncutRepository.findPublicGalleryWithUser(pageable);
    if (ncutPage.isEmpty()) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }
    return MyGalleryResponse.fromWithUser(ncutPage);
  }

  @Override
  public MyGalleryResponse getFollowingsGallery(String userUuid, int pageNum, int display) {
    if (userUuid == null || userUuid.isBlank()) {
      throw new BusinessException(ErrorCode.BAD_REQUEST);
    }

    List<String> followingUuids = followQueryRepository.findFollowingUuids(userUuid);
    if (followingUuids.isEmpty()) {
      throw new BusinessException(ErrorCode.NOT_FOUND); // 팔로잉 유저 없음
    }

    List<Visibility> allowedVisibilities = List.of(Visibility.PUBLIC, Visibility.FOLLOW);
    Pageable pageable = PageRequest.of(pageNum, display);
    Page<Ncut> ncutPage = ncutRepository.findFollowingsGalleryWithUser(followingUuids,
        allowedVisibilities, pageable);
    if (ncutPage.isEmpty()) {
      throw new BusinessException(ErrorCode.NOT_FOUND);
    }

    return MyGalleryResponse.fromWithUser(ncutPage);
  }
}
