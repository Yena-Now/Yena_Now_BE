package com.example.yenanow.s3.service;

public interface S3Service {

    String generatePresignedUploadUrl(String key, String contentType);

    String getFileUrl(String key);

    boolean deleteObject(String key);
}
