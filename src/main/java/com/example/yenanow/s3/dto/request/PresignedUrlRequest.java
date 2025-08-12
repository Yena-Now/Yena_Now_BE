package com.example.yenanow.s3.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PresignedUrlRequest {

    @Schema(description = "파일 타입", example = "frame", required = true)
    private String type;

    @Schema(description = "파일명", example = "sample.png", required = true)
    private String fileName;

    @Schema(description = "콘텐츠 타입", example = "image/png", required = true)
    private String contentType;

    @Schema(description = "릴레이 UUID (nullable)", example = "null 또는 UUID 값", nullable = true)
    private String relayUuid;

    @Schema(description = "룸 UUID (nullable)", example = "null 또는 UUID 값", nullable = true)
    private String roomCode;
}
