package com.example.yenanow.gallery.service;

import com.example.yenanow.gallery.dto.response.MyGalleryResponse;

public interface GalleryService {

  MyGalleryResponse getMyGallery(String userUuid, int page, int size);

  MyGalleryResponse getOtherGallery(String userUuid, int page, int size);

  MyGalleryResponse getPublicGallery(int page, int size);

  MyGalleryResponse getFollowingsGallery(String userUuid, int page, int size);
}
