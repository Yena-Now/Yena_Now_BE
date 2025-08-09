package com.example.yenanow.s3.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Presigner presigner;
    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${cloud.aws.region.static}")
    private String region;

    @Override
    public String generatePresignedUploadUrl(String key, String contentType) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(key)
            .contentType(contentType)
            .build();

        PresignedPutObjectRequest presignedRequest = presigner.presignPutObject(
            PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(5))
                .putObjectRequest(putObjectRequest)
                .build()
        );

        return presignedRequest.url().toString();
    }

    @Override
    public String getFileUrl(String key) {
        // null값 방어
        if (key == null || key.isBlank()) {
            return null;
        }
        // 주소로 왔다면 그대로 반환
        if (key.startsWith("http://") || key.startsWith("https://")) {
            return key;
        }
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + key;

    }

    @Override
    public void deleteObject(String key) {
        try {
            s3Client.deleteObject(b -> b.bucket(bucketName).key(key));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_DELETE_FAILED);
        }
    }

    @Override
    public void copyObject(String sourceKey, String destKey) {
        try {
            s3Client.copyObject(b -> b
                .sourceBucket(bucketName)
                .sourceKey(sourceKey)
                .destinationBucket(bucketName)
                .destinationKey(destKey));
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.S3_COPY_FAILED);
        }
    }
}