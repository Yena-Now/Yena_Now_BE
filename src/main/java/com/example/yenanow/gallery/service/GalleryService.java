package com.example.yenanow.gallery.service;

import com.example.yenanow.gallery.dto.request.UpdateNcutContentRequest;
import com.example.yenanow.gallery.dto.response.MyGalleryResponse;
import com.example.yenanow.gallery.dto.response.NcutDetailResponse;
import com.example.yenanow.gallery.dto.response.UpdateNcutContentResponse;

public interface GalleryService {

    // 나의 갤러리
    MyGalleryResponse getMyGallery(String userUuid, int page, int size);

    // 타인 갤러리
    MyGalleryResponse getOtherGallery(String userUuid, int page, int size);

    // 공개 갤러리
    MyGalleryResponse getPublicGallery(int page, int size);

    // 친구 갤러리
    MyGalleryResponse getFollowingsGallery(String userUuid, int page, int size);

    NcutDetailResponse getNcut(String userUuid, String ncutUuid);

    void deleteNcut(String userUuid, String ncutUuid);

    UpdateNcutContentResponse updateNcutContent(String userUuid, String ncutUuid,
        UpdateNcutContentRequest updateNcutContentRequest);
}
