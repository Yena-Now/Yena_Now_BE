package com.example.yenanow.s3.util;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import java.net.URLDecoder;
import java.net.URI;
import java.nio.charset.StandardCharsets;
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
            case "ncut" ->
                "ncut/%s/%s%s".formatted(LocalDate.now(ZoneId.of("Asia/Seoul")), UUID.randomUUID(),
                    ext);
            case "ncutThumbnail" ->
                "ncut/thumbnail/%s/%s%s".formatted(userUuid, UUID.randomUUID(), ext);
            case "cut" ->
                "cut/%s/%s/%s%s".formatted(LocalDate.now(ZoneId.of("Asia/Seoul")), roomCode,
                    UUID.randomUUID(), ext);
            case "yena" ->
                "cut/%s/%s/result/%s%s".formatted(LocalDate.now(ZoneId.of("Asia/Seoul")), roomCode,
                    UUID.randomUUID(), ext);
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
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            String rawPath = uri.getRawPath();
            if (rawPath == null || rawPath.isBlank()) {
                throw new IllegalArgumentException("S3 URL에 path가 없습니다.");
            }

            String path = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;

            // virtual-hosted 스타일이므로 아래 조건을 통과하지 않아야 정상
            if (host != null &&
                    (host.equals("s3.amazonaws.com")
                            || host.matches("^s3[.-][a-z0-9-]+\\.amazonaws\\.com$")
                            || host.equals("s3-accelerate.amazonaws.com"))) {

                int firstSlash = path.indexOf('/');
                if (firstSlash < 0) {
                    throw new IllegalArgumentException("경로식 S3 URL 형식이 올바르지 않습니다.");
                }
                path = path.substring(firstSlash + 1);
            }

            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("지원하지 않는 S3 URL: " + url, e);
        }
    }

    // NCut 결과물: ncut/{UUID}.{ext}
    public String createNcutKey(String userUuid, String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        if (ext == null || ext.isBlank()) {
            ext = "bin";
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return "ncut/%s/%s.%s".formatted(today, UUID.randomUUID(), ext.toLowerCase());
    }

    // Ncut 썸네일: ncut/thumbnail/{userUuid}/{UUID}.{ext}
    public String createNcutThumbnailKey(String userUuid, String fileName) {
        String ext = FilenameUtils.getExtension(fileName);
        if (ext == null || ext.isBlank()) {
            ext = "bin";
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return "ncut/thumbnail/%s/%s/%s.%s".formatted(
            userUuid, today, UUID.randomUUID(), ext.toLowerCase());
    }

    // 촬영 컷: cut/{roomCode}/{UUID}.{ext}
    public String createCutKey(String roomCode, String fileName, Integer cutIndex) {
        String ext = FilenameUtils.getExtension(fileName);
        if (ext == null || ext.isBlank()) {
            ext = "bin";
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        String leaf = "%s.%s".formatted(UUID.randomUUID(), ext.toLowerCase());
        return "cut/%s/%s/%s".formatted(today, roomCode, leaf);
    }
}