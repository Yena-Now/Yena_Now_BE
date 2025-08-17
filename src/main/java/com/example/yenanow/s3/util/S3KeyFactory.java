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
            String rawPath = uri.getRawPath();  // 쿼리/프래그먼트 제외
            if (rawPath == null || rawPath.isBlank()) {
                throw new IllegalArgumentException("S3 URL에 path가 없습니다.");
            }

            // 선행 슬래시 제거
            String path = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;

            // 경로식(URL이 s3.* 호스트)인 경우에만 첫 세그먼트(버킷) 제거
            if (host != null &&
                    (host.equals("s3.amazonaws.com")
                            || host.startsWith("s3.")
                            || host.startsWith("s3-accelerate"))) {
                int firstSlash = path.indexOf('/');
                if (firstSlash < 0) {
                    throw new IllegalArgumentException("경로식 S3 URL 형식이 올바르지 않습니다. (bucket/key 필요)");
                }
                path = path.substring(firstSlash + 1);
            }

            // URL 디코딩 (예: a%2Fb%2Fc.jpg -> a/b/c.jpg)
            return URLDecoder.decode(path, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw e;
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