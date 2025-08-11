package com.example.yenanow.s3.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.film.service.FilmService;
import com.example.yenanow.s3.util.S3KeyFactory;
import com.example.yenanow.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UploadDbSaveService {

    private final UserService userService;
    private final FilmService filmService;
    // private final GalleryService galleryService;
    // private final RelayCutService relayCutService;
    private final S3KeyFactory s3KeyFactory;

    public void saveUrl(String type, String fileUrl, String key, String userUuid, String relayUuid,
        String ncutUuid, String roodCode) {
        String s3Key = (key != null && !key.isBlank()) ? key : extractKeyOrThrow(fileUrl);
        switch (type) {
            case "profile" -> userService.updateProfileUrl(userUuid, fileUrl);
            case "background" -> filmService.createBackground(s3Key);
            case "ncut" -> {
                require(userUuid != null && !userUuid.isBlank(), "ncut타입은 userUuid가 필요합니다.");
//                ncutService.saveNcut(fileUrl, userUuid);
            }
            case "ncutThumbnail" -> {
                require(ncutUuid != null && !ncutUuid.isBlank(),
                    "ncutThumbnail타입은 ncutUuid가 필요합니다.");
//                ncutService.updateThumbnail(ncutUuid, fileUrl);
            }
            case "relayCut" -> {
                require(relayUuid != null && !relayUuid.isBlank(),
                    "relayUuid타입은 relayUuid가 필요합니다.");
//                relayCutService.saveRelayCut(relayUuid, fileUrl);
            }
            default -> throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }

    private String extractKeyOrThrow(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
        return s3KeyFactory.extractKeyFromUrl(fileUrl);
    }

    private void require(boolean cond, String message) {
        if (!cond) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
