package com.example.yenanow.s3.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

@Configuration
public class S3Config {

    // ap-northeast-2 등
    @Value("${cloud.aws.region.static}")
    private String region;

    // IAM Access Key (application.properties)
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    // IAM Secret Key (application.properties)
    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    // 공통 자격 증명 객체
    private StaticCredentialsProvider credentials() {
        return StaticCredentialsProvider.create(
            AwsBasicCredentials.create(accessKey, secretKey)
        );
    }

    // S3 동기 클라이언트
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
            .region(Region.of(region))
            .credentialsProvider(credentials())
            .build();
    }

    // Presigned URL 생성기
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
            .region(Region.of(region))
            .credentialsProvider(credentials())
            .build();
    }
}