package com.example.yenanow.s3.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UploadDbSaveService {

    private final UserRepository userRepository;
//    private final FrameRepository frameRepository;
//    private final BackgroundRepository backgroundRepository;
//    private final StickerRepository stickerRepository;
//    private final NcutRepository ncutRepository;
//    private final RelayCutRepository relayCutRepository;

    public void saveUrl(String type, String fileUrl, String key, String userUuid, String relayUuid,
        String ncutUuid) {
        switch (type) {
            case "profile":
                User user = userRepository.findById(userUuid)
                    .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER_PROFILE));
                user.setProfileUrl(fileUrl);
                userRepository.save(user);
                break;
//            case "profile" -> userService.updateProfileUrl(userUuid, fileUrl);
//            case "frame" -> frameService.saveFrame(fileUrl);
//            case "background" -> backgroundService.saveBackground(fileUrl);
//            case "sticker" -> stickerService.saveSticker(fileUrl);
//            case "ncut" -> ncutService.saveNcut(fileUrl, userUuid);
//            case "ncut-thumbnail" -> ncutService.updateThumbnail(ncutUuid, fileUrl);
//            case "relay-cut" -> relayCutService.saveRelayCut(relayUuid, fileUrl);
//            default -> throw new BusinessException(ErrorCode.BAD_REQUEST);
            default:
                throw new IllegalArgumentException("지원하지 않는 type: " + type);
        }
    }
}
