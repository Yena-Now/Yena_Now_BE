package com.example.yenanow.gallery.controller;

import com.example.yenanow.gallery.dto.response.MyGalleryResponse;
import com.example.yenanow.gallery.service.GalleryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    @GetMapping("/me")
    public ResponseEntity<MyGalleryResponse> getMyGallery(
        @AuthenticationPrincipal Object principal,  // JWT 필터에서 세팅한 UUID 문자열
        @RequestParam(defaultValue = "0") int pageNum,
        @RequestParam(defaultValue = "30") int display) {

        String userUuid = principal.toString();
        MyGalleryResponse response = galleryService.getMyGallery(userUuid, pageNum, display);
        return ResponseEntity.ok(response);
    }
}
