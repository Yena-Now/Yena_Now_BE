package com.example.yenanow.gallery.controller;

import com.example.yenanow.gallery.dto.request.UpdateNcutContentRequest;
import com.example.yenanow.gallery.dto.request.UpdateNcutVisibilityRequest;
import com.example.yenanow.gallery.dto.response.MyGalleryResponse;
import com.example.yenanow.gallery.dto.response.NcutDetailResponse;
import com.example.yenanow.gallery.dto.response.NcutLikeResponse;
import com.example.yenanow.gallery.dto.response.NcutLikesResponse;
import com.example.yenanow.gallery.dto.response.UpdateNcutContentResponse;
import com.example.yenanow.gallery.dto.response.UpdateNcutVisibilityResponse;
import com.example.yenanow.gallery.service.GalleryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Gallery", description = "갤러리 관련 API")
@RestController
@RequestMapping("/gallery")
@RequiredArgsConstructor
public class GalleryController {

    private final GalleryService galleryService;

    @Operation(summary = "내 갤러리 조회", description = "로그인한 사용자의 개인 갤러리를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<MyGalleryResponse> getMyGallery(
        @Parameter(hidden = true)
        @AuthenticationPrincipal Object principal,
        @Parameter(description = "페이지 번호 (기본값: 0)")
        @RequestParam(defaultValue = "0") int pageNum,
        @Parameter(description = "페이지당 표시할 개수 (기본값: 30)")
        @RequestParam(defaultValue = "30") int display) {

        String userUuid = principal.toString();
        MyGalleryResponse response = galleryService.getMyGallery(userUuid, pageNum, display);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "특정 유저 공개 갤러리 조회", description = "지정한 사용자 UUID의 공개된 갤러리를 조회합니다.")
    @GetMapping("/{userUuid}")
    public ResponseEntity<MyGalleryResponse> getOtherGallery(
        @Parameter(description = "조회할 대상 사용자 UUID", required = true)
        @PathVariable String userUuid,
        @Parameter(description = "페이지 번호 (기본값: 0)")
        @RequestParam(defaultValue = "0") int pageNum,
        @Parameter(description = "페이지당 표시할 개수 (기본값: 30)")
        @RequestParam(defaultValue = "30") int display) {

        MyGalleryResponse response = galleryService.getOtherGallery(userUuid, pageNum, display);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "전체 공개 갤러리 조회", description = "모든 사용자들의 전체 공개된 갤러리를 조회합니다.")
    @GetMapping("/public")
    public ResponseEntity<MyGalleryResponse> getPublicGallery(
        @Parameter(description = "페이지 번호 (기본값: 0)")
        @RequestParam(defaultValue = "0") int pageNum,
        @Parameter(description = "페이지당 표시할 개수 (기본값: 30)")
        @RequestParam(defaultValue = "30") int display) {

        MyGalleryResponse response = galleryService.getPublicGallery(pageNum, display);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "팔로잉 유저 갤러리 조회", description = "로그인한 사용자가 팔로우하는 유저들의 최신 갤러리를 조회합니다.")
    @GetMapping("/followings")
    public ResponseEntity<MyGalleryResponse> getFollowingsGallery(
        @Parameter(hidden = true)
        @AuthenticationPrincipal Object principal,
        @Parameter(description = "페이지 번호 (기본값: 0)")
        @RequestParam(defaultValue = "0") int pageNum,
        @Parameter(description = "페이지당 표시할 개수 (기본값: 30)")
        @RequestParam(defaultValue = "30") int display) {

        String userUuid = principal.toString();
        MyGalleryResponse response = galleryService.getFollowingsGallery(userUuid, pageNum,
            display);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/ncuts/{ncutUuid}")
    public ResponseEntity<NcutDetailResponse> getNcut(
        @AuthenticationPrincipal Object principal,
        @PathVariable("ncutUuid") String ncutUuid) {

        String userUuid = principal.toString();
        NcutDetailResponse ncutDetailResponse = galleryService.getNcut(userUuid, ncutUuid);
        return ResponseEntity.ok(ncutDetailResponse);
    }

    @DeleteMapping("/ncuts/{ncutUuid}")
    public ResponseEntity<Void> deleteNcut(
        @AuthenticationPrincipal Object principal,
        @PathVariable("ncutUuid") String ncutUuid) {

        String userUuid = principal.toString();
        galleryService.deleteNcut(userUuid, ncutUuid);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/ncuts/{ncutUuid}/content")
    public ResponseEntity<UpdateNcutContentResponse> updateNcutContent(
        @AuthenticationPrincipal Object principal,
        @PathVariable("ncutUuid") String ncutUuid,
        @RequestBody UpdateNcutContentRequest updateNcutContentRequest) {

        String userUuid = principal.toString();
        UpdateNcutContentResponse updateNcutContentResponse = galleryService.updateNcutContent(
            userUuid, ncutUuid, updateNcutContentRequest);
        return ResponseEntity.ok(updateNcutContentResponse);
    }

    @PatchMapping("/ncuts/{ncutUuid}/visibility")
    public ResponseEntity<UpdateNcutVisibilityResponse> updateNcutvisibility(
        @AuthenticationPrincipal Object principal,
        @PathVariable("ncutUuid") String ncutUuid,
        @RequestBody UpdateNcutVisibilityRequest updateNcutVisibilityRequest) {

        String userUuid = principal.toString();
        UpdateNcutVisibilityResponse updateNcutVisibilityResponse = galleryService.updateNcutVisibility(
            userUuid, ncutUuid, updateNcutVisibilityRequest);
        return ResponseEntity.ok(updateNcutVisibilityResponse);
    }

    @GetMapping("/ncuts/{ncutUuid}/likes")
    public ResponseEntity<NcutLikesResponse> getNcutLikes(
        @AuthenticationPrincipal Object principal,
        @PathVariable("ncutUuid") String ncutUuid,
        @RequestParam("pageNum") int pageNum,
        @RequestParam("display") int display) {

        String userUuid = principal.toString();
        NcutLikesResponse ncutLikesResponse = galleryService.getNcutLikes(userUuid, ncutUuid,
            pageNum, display);
        return ResponseEntity.ok(ncutLikesResponse);
    }

    @PostMapping("/ncuts/{ncutUuid}/likes")
    public ResponseEntity<NcutLikeResponse> createNcutLike(
        @AuthenticationPrincipal Object principal,
        @PathVariable("ncutUuid") String ncutUuid) {

        String userUuid = principal.toString();
        NcutLikeResponse ncutLikeResponse = galleryService.createNcutLike(userUuid, ncutUuid);
        return ResponseEntity.ok(ncutLikeResponse);
    }
}
