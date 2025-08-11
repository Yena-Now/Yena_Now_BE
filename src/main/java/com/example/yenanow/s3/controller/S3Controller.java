package com.example.yenanow.s3.controller;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.util.UuidUtil;
import com.example.yenanow.s3.dto.request.PresignedUrlRequest;
import com.example.yenanow.s3.dto.response.PresignedUrlResponse;
import com.example.yenanow.s3.service.S3Service;
import com.example.yenanow.s3.service.UploadDbSaveService;
import com.example.yenanow.s3.util.S3KeyFactory;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@Tag(name = "S3", description = "S3 Presigned URL 발급 및 업로드 기록 API")
@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;
    private final S3KeyFactory s3KeyFactory;
    private final UploadDbSaveService uploadDbSaveService;

    @Operation(
        summary = "S3 Presigned URL 발급",
        description = "S3에 파일 업로드 시 사용할 Presigned URL을 발급합니다.<br>"
            + "일부 타입은 DB 저장 없이 Presigned URL만 발급합니다."
    )
    @PostMapping("/presigned-url")
    public ResponseEntity<PresignedUrlResponse> getPresignedUrl(
        @RequestBody PresignedUrlRequest request,
        @Parameter(hidden = true)
        @AuthenticationPrincipal Object principal
    ) {
        if (request == null) {
            throw new BusinessException(ErrorCode.EMPTY_REQUEST_BODY);
        }

        final String type = request.getType();
        final String fileName = request.getFileName();
        final String contentType = request.getContentType();

        if ("ncut".equals(type)) {
            return ResponseEntity.ok(
                uploadDbSaveService.presignForNcut(fileName, contentType)
            );
        }

        if ("ncutThumbnail".equals(type)) {
            String userUuid = principal.toString();
            UuidUtil.validateUuid(userUuid);
            return ResponseEntity.ok(
                uploadDbSaveService.presignForNcutThumbnail(userUuid, fileName, contentType)
            );
        }

        if ("cut".equals(type)) {
            return ResponseEntity.ok(
                uploadDbSaveService.presignForCut(request.getRoomCode(), fileName, contentType)
            );
        }

        String userUuid = principal.toString();
        UuidUtil.validateUuid(userUuid);

        // S3 Key 생성
        String key = s3KeyFactory.createKey(
            type,
            fileName,
            userUuid,
            request.getRelayUuid(),
            request.getRoomCode()
        );

        // 업로드 URL 및 DB 저장
        String fileUrl = s3Service.getFileUrl(key);

        uploadDbSaveService.saveUrl(
            type,
            fileUrl,
            key,
            userUuid,
            request.getRelayUuid(),
            request.getNcutUuid(),
            request.getRoomCode()
        );

        // Presigned URL 생성
        String uploadUrl = s3Service.generatePresignedUploadUrl(key, contentType);

        return ResponseEntity.ok(new PresignedUrlResponse(uploadUrl, fileUrl));
    }

    @Operation(summary = "S3 객체 삭제")
    @DeleteMapping
    public ResponseEntity<Void> deleteFile(
        @Parameter(description = "S3 객체 키", required = true)
        @RequestParam String key
    ) {
        s3Service.deleteObject(key);
        return ResponseEntity.noContent().build();
    }
}
