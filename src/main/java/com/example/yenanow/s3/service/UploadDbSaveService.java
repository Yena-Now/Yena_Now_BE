package com.example.yenanow.s3.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.film.service.FilmService;
import com.example.yenanow.openvidu.service.OpenviduService;
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
    private final OpenviduService openviduService;

    public void saveUrl(String type, String fileUrl, String userUuid) {
        switch (type) {
            case "profile" -> userService.updateProfileUrl(userUuid, fileUrl);
            case "background" -> {
            }
            case "ncut" -> {
            }
            case "ncutThumbnail" -> {
            }
            case "cut" -> {
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
        String fileUrl = s3Service.getFileUrl(key);
        return new PresignedUrlResponse(uploadUrl, fileUrl);
    }

    // ncut 썸네일: ncut/thumbnail/{userUuid}/{UUID}.{ext} (경로 규칙 유지)
    public PresignedUrlResponse presignForNcutThumbnail(String userUuid, String fileName,
        String contentType) {
        require(userUuid != null && !userUuid.isBlank(), "userUuid required");
        require(fileName != null && !fileName.isBlank(), "fileName required");
        require(contentType != null && !contentType.isBlank(), "contentType required");

        String key = s3KeyFactory.createNcutThumbnailKey(userUuid, fileName);
        String uploadUrl = s3Service.generatePresignedUploadUrl(key, contentType);
        String fileUrl = s3Service.getFileUrl(key);
        return new PresignedUrlResponse(uploadUrl, fileUrl);
    }

    // 촬영 컷: cut/{roomCode}/{UUID}.{ext} (인덱스 없음)
    public PresignedUrlResponse presignForCut(String roomCode, String fileName,
        String contentType) {
        require(roomCode != null && !roomCode.isBlank(), "roomCode required");
        require(fileName != null && !fileName.isBlank(), "fileName required");
        require(contentType != null && !contentType.isBlank(), "contentType required");

        // 1) 프리사인드에 사용할 S3 key 생성
        String key = s3KeyFactory.createCutKey(roomCode, fileName, null);

        // 2) Redis room:{roomCode}.cuts 에 key 추가 (프리사인드 '요청 시점'에 반영)
        openviduService.addCutKeyToRoom(roomCode, key);

        // 3) Presigned URL & 파일 URL 생성
        String uploadUrl = s3Service.generatePresignedUploadUrl(key, contentType);
        String fileUrl = s3Service.getFileUrl(key);

        return new PresignedUrlResponse(uploadUrl, fileUrl);
    }

    private void require(boolean cond, String message) {
        if (!cond) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
