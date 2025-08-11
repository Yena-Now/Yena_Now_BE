package com.example.yenanow.film.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.film.dto.response.BackgroundListResponse;
import com.example.yenanow.film.dto.response.FrameListResponse;
import com.example.yenanow.film.dto.response.StickerListResponse;
import com.example.yenanow.film.dto.response.StickerListResponseItem;
import com.example.yenanow.film.entity.Background;
import com.example.yenanow.film.entity.Sticker;
import com.example.yenanow.film.repository.BackgroundRepository;
import com.example.yenanow.film.repository.FrameRepository;
import com.example.yenanow.film.repository.StickerRepository;
import com.example.yenanow.s3.service.S3Service;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FilmServiceImpl implements FilmService {

    private final FrameRepository frameRepository;
    private final StickerRepository stickerRepository;
    private final BackgroundRepository backgroundRepository;
    private final S3Service s3Service;

    @Override
    public void createBackground(String s3Key) {
        require(s3Key, "s3Key");
        Background background = new Background();
        background.setBackgroundUrl(s3Key);
        background.setBackgroundName(UUID.randomUUID().toString());
        backgroundRepository.save(background);
    }

    @Override
    public List<FrameListResponse> getFrames() {
        return frameRepository.findAll().stream()
            .map(frame -> FrameListResponse.builder()
                .frameUuid(frame.getFrameUuid())
                .frameName(frame.getFrameName())
                .frameUrl(s3Service.getFileUrl(frame.getFrameUrl())) // Key → URL 변환
                .frameCut(frame.getFrameCut())
                .frameType(frame.getFrameType())
                .build())
            .toList();
    }

    @Override
    public StickerListResponse getStickers(Pageable pageable) {
        Page<Sticker> stickerPage = stickerRepository.findAll(pageable);

        List<StickerListResponseItem> stickerListResponseItems = stickerPage.getContent().stream()
            .map(sticker -> StickerListResponseItem.builder()
                .stickerUuid(sticker.getStickerUuid())
                .stickerName(sticker.getStickerName())
                .stickerUrl(s3Service.getFileUrl(sticker.getStickerUrl())) // Key → URL 변환
                .build())
            .toList();

        return StickerListResponse.builder()
            .totalPages(stickerPage.getTotalPages())
            .stickers(stickerListResponseItems)
            .build();
    }

    @Override
    public List<BackgroundListResponse> getBackgrounds() {
        return backgroundRepository.findAll().stream()
            .map(background -> BackgroundListResponse.builder()
                .backgroundUuid(background.getBackgroundUuid())
                .backgroundName(background.getBackgroundName())
                .backgroundUrl(s3Service.getFileUrl(background.getBackgroundUrl())) // Key → URL 변환
                .build())
            .toList();
    }


    private void require(String v, String name) {
        if (v == null || v.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
