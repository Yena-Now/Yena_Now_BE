package com.example.yenanow.s3.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.film.service.FilmService;
import com.example.yenanow.s3.dto.response.PresignedUrlResponse;
import com.example.yenanow.s3.util.S3KeyFactory;
import com.example.yenanow.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UploadDbSaveService {

    private final UserService userService;
    private final FilmService filmService;
    // private final GalleryService galleryService;
    // private final RelayCutService relayCutService;
    private final S3KeyFactory s3KeyFactory;
    private final S3Service s3Service;

    public void saveUrl(String type, String fileUrl, String key, String userUuid, String relayUuid,
        String ncutUuid, String roomCode) {
        String s3Key = (key != null && !key.isBlank()) ? key : extractKeyOrThrow(fileUrl);
        switch (type) {
            case "profile" -> userService.updateProfileUrl(userUuid, fileUrl);
            case "background" -> { } // filmService.createBackground(s3Key);
            case "ncut" -> { }
            case "ncutThumbnail" -> { }
            case "cut" -> { }
            case "relayCut" -> {
                require(relayUuid != null && !relayUuid.isBlank(),
                    "relayUuid타입은 relayUuid가 필요합니다.");
//                relayCutService.saveRelayCut(relayUuid, fileUrl);
            }
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }

    // ncut 결과물: ncut/{UUID}.{ext} (userUuid 경로 없음)
    public PresignedUrlResponse presignForNcut(String fileName, String contentType) {
        require(fileName != null && !fileName.isBlank(), "fileName required");
        require(contentType != null && !contentType.isBlank(), "contentType required");

        String key = s3KeyFactory.createNcutKey(null, fileName);
        String uploadUrl = s3Service.generatePresignedUploadUrl(key, contentType);
        String fileUrl   = s3Service.getFileUrl(key);
        return new PresignedUrlResponse(uploadUrl, fileUrl);
    }

    // ncut 썸네일: ncut/thumbnail/{userUuid}/{UUID}.{ext} (경로 규칙 유지)
    public PresignedUrlResponse presignForNcutThumbnail(String userUuid, String fileName, String contentType) {
        require(userUuid != null && !userUuid.isBlank(), "userUuid required");
        require(fileName != null && !fileName.isBlank(), "fileName required");
        require(contentType != null && !contentType.isBlank(), "contentType required");

        String key = s3KeyFactory.createNcutThumbnailKey(userUuid, fileName);
        String uploadUrl = s3Service.generatePresignedUploadUrl(key, contentType);
        String fileUrl   = s3Service.getFileUrl(key);
        return new PresignedUrlResponse(uploadUrl, fileUrl);
    }

    // 촬영 컷: cut/{roomCode}/{UUID}.{ext} (인덱스 없음)
    public PresignedUrlResponse presignForCut(String roomCode, String fileName, String contentType) {
        require(roomCode != null && !roomCode.isBlank(), "roomCode required");
        require(fileName != null && !fileName.isBlank(), "fileName required");
        require(contentType != null && !contentType.isBlank(), "contentType required");

        // cutIndex는 더 이상 사용하지 않으므로 null 전달
        String key = s3KeyFactory.createCutKey(roomCode, fileName, null);
        String uploadUrl = s3Service.generatePresignedUploadUrl(key, contentType);
        String fileUrl   = s3Service.getFileUrl(key);
        return new PresignedUrlResponse(uploadUrl, fileUrl);
    }

    private String extractKeyOrThrow(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return s3KeyFactory.extractKeyFromUrl(fileUrl);
    }

    private void require(boolean cond, String message) {
        if (!cond) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
