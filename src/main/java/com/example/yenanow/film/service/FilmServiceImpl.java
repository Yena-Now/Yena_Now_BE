package com.example.yenanow.film.service;

import com.example.yenanow.film.dto.response.BackgroundListResponse;
import com.example.yenanow.film.dto.response.FrameListResponse;
import com.example.yenanow.film.dto.response.StickerListResponse;
import com.example.yenanow.film.dto.response.StickerListResponseItem;
import com.example.yenanow.film.entity.Sticker;
import com.example.yenanow.film.repository.BackgroundRepository;
import com.example.yenanow.film.repository.FrameRepository;
import com.example.yenanow.film.repository.StickerRepository;
import java.util.List;
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

    @Override
    public List<FrameListResponse> getFrames() {
        return frameRepository.findAll().stream()
            .map(FrameListResponse::fromEntity)
            .toList();
    }

    @Override
    public StickerListResponse getStickers(Pageable pageable) {
        Page<Sticker> page = stickerRepository.findAll(pageable);
        return StickerListResponse.builder()
            .totalPages(page.getTotalPages())
            .stickers(page.getContent().stream()
                .map(StickerListResponseItem::fromEntity)
                .toList())
            .build();
    }

    @Override
    public List<BackgroundListResponse> getBackgrounds() {
        return backgroundRepository.findAll().stream()
            .map(BackgroundListResponse::fromEntity)
            .toList();
    }
}
