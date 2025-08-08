package com.example.yenanow.film.service;

import com.example.yenanow.film.dto.request.MergeRequest;
import com.example.yenanow.film.dto.response.BackgroundListResponse;
import com.example.yenanow.film.dto.response.FrameListResponse;
import com.example.yenanow.film.dto.response.MergeResponse;
import com.example.yenanow.film.dto.response.StickerListResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import org.springframework.data.domain.Pageable;


public interface FilmService {

    List<FrameListResponse> getFrames(int frameCut);

    StickerListResponse getStickers(Pageable pageable);

    List<BackgroundListResponse> getBackgrounds();

    CompletableFuture<MergeResponse> createMergedOutput(MergeRequest request, String userUuid);
}
