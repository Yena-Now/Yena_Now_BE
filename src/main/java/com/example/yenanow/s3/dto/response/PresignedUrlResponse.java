package com.example.yenanow.s3.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PresignedUrlResponse {

    // S3로 파일 업로드할 때 사용할 Presigned URL
    private String uploadUrl;

    // 업로드 완료 후 접근 가능한 URL
    private String fileUrl;
}