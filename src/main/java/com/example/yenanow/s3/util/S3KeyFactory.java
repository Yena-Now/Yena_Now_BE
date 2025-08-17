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

            // 1) path만 취득 (쿼리스트링/프래그먼트 제외)
            String rawPath = uri.getRawPath();
            if (rawPath == null || rawPath.isBlank()) {
                throw new IllegalArgumentException("S3 URL에 path가 없습니다.");
            }

            // 2) 선행 슬래시 제거 → "bucket/...." 형태를 기대
            String path = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;

            // 3) 경로식 URL은 첫 세그먼트가 버킷명 → 제거
            int firstSlash = path.indexOf('/');
            if (firstSlash < 0) {
                throw new IllegalArgumentException("경로식 S3 URL 형식이 올바르지 않습니다. (bucket/key 필요)");
            }
            String keyEncoded = path.substring(firstSlash + 1);
            if (keyEncoded.isBlank()) {
                throw new IllegalArgumentException("S3 key가 비어 있습니다.");
            }

            // 4) URL 디코딩 (예: a%2Fb%2Fc.jpg → a/b/c.jpg)
            return URLDecoder.decode(keyEncoded, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("지원하지 않는 경로식 S3 URL: " + url, e);
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