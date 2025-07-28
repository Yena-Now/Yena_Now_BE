package com.example.yenanow.gallery.service;

import com.example.yenanow.gallery.dto.response.MyGalleryResponse;

public interface GalleryService {

    MyGalleryResponse getMyGallery(String userUuid, int pageNum, int display);
}