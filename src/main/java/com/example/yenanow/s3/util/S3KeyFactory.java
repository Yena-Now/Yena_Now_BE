package com.example.yenanow.s3.util;

import java.util.UUID;
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
}