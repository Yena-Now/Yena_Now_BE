package com.example.yenanow.s3.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

@Component
public class S3KeyFactory {

    /* 범용 키 생성(기존) -------------------------------------------------- */

    public String createKey(String type, String fileName,
        String userUuid, String roomCode) {
        String ext = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > -1) {
            ext = fileName.substring(dotIndex);
        }

        return switch (type) {
            case "profile" -> "profile/%s/%s%s".formatted(userUuid, UUID.randomUUID(), ext);
            case "background" -> "background/user/%s/%s%s"
                .formatted(LocalDate.now(ZoneId.of("Asia/Seoul")), UUID.randomUUID(), ext);
            case "ncut" -> "ncut/%s%s".formatted(UUID.randomUUID(), ext);
            case "ncutThumbnail" ->
                "ncut/thumbnail/%s/%s%s".formatted(userUuid, UUID.randomUUID(), ext);
            case "cut" -> "cut/%s/%s%s".formatted(roomCode, UUID.randomUUID(), ext);
            case "yena" -> "cut/%s/result/%s%s".formatted(roomCode, UUID.randomUUID(), ext);
            default -> throw new IllegalArgumentException("지원하지 않는 type: " + type);
        };
    }

    /* 회원가입 전용 ------------------------------------------------------- */

    public String createTempProfileKey(String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        return "profile/temp/%s.%s".formatted(UUID.randomUUID(), ext);
    }

    public String createProfileKeyWithoutUser(String ext) {
        return "profile/%s/%s.%s".formatted(LocalDate.now(ZoneId.of("Asia/Seoul")),
            UUID.randomUUID(), ext);
    }

    public String extractKeyFromUrl(String url) {
        int idx = url.indexOf(".amazonaws.com/");
        if (idx == -1) {
            throw new IllegalArgumentException("잘못된 S3 URL");
        }
        return url.substring(idx + ".amazonaws.com/".length());
    }

    // NCut 결과물: ncut/{UUID}.{ext}
    public String createNcutKey(String userUuid, String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        if (ext == null || ext.isBlank()) {
            ext = "bin";
        }
        return "ncut/%s.%s".formatted(java.util.UUID.randomUUID(), ext.toLowerCase());
    }

    // Ncut 썸네일: ncut/thumbnail/{userUuid}/{UUID}.{ext}
    public String createNcutThumbnailKey(String userUuid, String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        if (ext == null || ext.isBlank()) {
            ext = "bin";
        }
        return "ncut/thumbnail/%s/%s.%s".formatted(userUuid, UUID.randomUUID(), ext.toLowerCase());
    }

    // 촬영 컷: cut/{roomCode}/{UUID}.{ext}
    public String createCutKey(String roomCode, String fileName, Integer cutIndex) {
        String ext = FilenameUtils.getExtension(fileName);
        if (ext == null || ext.isBlank()) {
            ext = "bin";
        }
        String leaf = String.format("%s.%s", java.util.UUID.randomUUID(), ext.toLowerCase());
        return "cut/%s/%s".formatted(roomCode, leaf);
    }
}