package com.example.yenanow.s3.controller;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.s3.dto.request.PresignedUrlRequest;
import com.example.yenanow.s3.dto.response.PresignedUrlResponse;
import com.example.yenanow.s3.service.S3Service;
import com.example.yenanow.s3.service.UploadDbSaveService;
import com.example.yenanow.s3.util.S3KeyFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;
    private final S3KeyFactory s3KeyFactory;
    private final UploadDbSaveService uploadDbSaveService;

    @GetMapping("/s3/presigned-url")
    public PresignedUrlResponse getPresignedUrl(
        @Valid PresignedUrlRequest request,
        @AuthenticationPrincipal Object principal
    ) {
        String userUuid = principal.toString();
        if (userUuid == null) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }

        // Key 생성
        String key = s3KeyFactory.createKey(
            request.getType(), request.getFileName(), userUuid, request.getRelayUuid());

        // DB 저장
        uploadDbSaveService.saveUrl(
            request.getType(), s3Service.getFileUrl(key), key,
            userUuid, request.getRelayUuid(), request.getNcutUuid());

        // Presigned URL 발급
        String uploadUrl = s3Service.generatePresignedUploadUrl(key, request.getContentType());

        return new PresignedUrlResponse(uploadUrl, s3Service.getFileUrl(key));
    }
}
