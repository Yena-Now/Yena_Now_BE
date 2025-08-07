package com.example.yenanow.s3.util;

import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

@Component
public class S3KeyFactory {

    /* 범용 키 생성(기존) -------------------------------------------------- */

    public String createKey(String type, String fileName,
        String userUuid, String relayUuid) {
        String ext = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > -1) {
            ext = fileName.substring(dotIndex);
        }

        return switch (type) {
            case "profile" -> "profile/%s/%s%s".formatted(userUuid, UUID.randomUUID(), ext);
            case "frame" -> "frame/%s%s".formatted(UUID.randomUUID(), ext);
            case "background" -> "background/%s%s".formatted(UUID.randomUUID(), ext);
            case "sticker" -> "sticker/%s%s".formatted(UUID.randomUUID(), ext);
            case "ncut" -> "ncut/%s/%s%s".formatted(userUuid, UUID.randomUUID(), ext);
            case "ncutThumbnail" ->
                "ncut/thumbnail/%s/%s%s".formatted(userUuid, UUID.randomUUID(), ext);
            case "relayCut" -> "relay/%s/%s%s".formatted(relayUuid, UUID.randomUUID(), ext);
            default -> throw new IllegalArgumentException("지원하지 않는 type: " + type);
        };
    }

    /* 회원가입 전용 ------------------------------------------------------- */

    /**
     * temp 프로필 키: profile/temp/랜덤.ext
     */
    public String createTempProfileKey(String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        return "profile/temp/%s.%s".formatted(UUID.randomUUID(), ext);
    }

    /**
     * 프로필 최종 키: profile/{userUuid}/{랜덤}.{ext}
     */
    public String createFinalProfileKey(String userUuid, String ext) {
        return "profile/%s/%s.%s".formatted(userUuid, UUID.randomUUID(), ext);
    }

    /**
     * https://bucket.s3…/profile/temp/abc.jpg → profile/temp/abc.jpg
     */
    public String extractKeyFromUrl(String url) {
        int idx = url.indexOf(".amazonaws.com/");
        if (idx == -1) {
            throw new IllegalArgumentException("잘못된 S3 URL");
        }
        return url.substring(idx + ".amazonaws.com/".length());
    }
}