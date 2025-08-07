package com.example.yenanow.s3.util;

import java.util.UUID;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Component;

@Component
public class S3KeyFactory {

    public String createKey(String type, String fileName, String userUuid, String relayUuid) {
        String ext = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex > -1) {
            ext = fileName.substring(dotIndex);
        }

        switch (type) {
            case "profile":
                return "profile/" + userUuid + "/" + UUID.randomUUID() + ext;
            case "frame":
                return "frame/" + UUID.randomUUID() + ext;
            case "background":
                return "background/" + UUID.randomUUID() + ext;
            case "sticker":
                return "sticker/" + UUID.randomUUID() + ext;
            case "ncut":
                return "ncut/" + userUuid + "/" + UUID.randomUUID() + ext;
            case "ncutThumbnail":
                return "ncut/thumbnail/" + userUuid + "/" + UUID.randomUUID() + ext;
            case "relayCut":
                return "relay/" + relayUuid + "/" + UUID.randomUUID() + ext;
            default:
                throw new IllegalArgumentException("지원하지 않는 type: " + type);
        }
    }

    /** 회원가입 단계: temp 프로필 키 생성 */
    public String createTempProfileKey(String fileName) {
        String ext = FilenameUtils.getExtension(fileName);      // jpg, png …
        return "profile/temp/%s.%s".formatted(UUID.randomUUID(), ext);
    }

    /** 회원가입 완료 후: 최종 프로필 키 */
    public String createFinalProfileKey(String userUuid) {
        return "profile/%s.jpg".formatted(userUuid);
    }

    /** https://bucket.s3.…/profile/temp/abc.jpg → profile/temp/abc.jpg */
    public String extractKeyFromUrl(String url) {
        int idx = url.indexOf(".amazonaws.com/");
        if (idx == -1) throw new IllegalArgumentException("잘못된 S3 URL");
        return url.substring(idx + ".amazonaws.com/".length());
    }
}