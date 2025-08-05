package com.example.yenanow.film.service;

import com.example.yenanow.film.dto.response.BackgroundListResponse;
import com.example.yenanow.film.dto.response.FrameListResponse;
import com.example.yenanow.film.dto.response.StickerListResponse;
import java.util.List;
import org.springframework.data.domain.Pageable;


public interface FilmService {

    List<FrameListResponse> getFrames();

    StickerListResponse getStickers(Pageable pageable);

    List<BackgroundListResponse> getBackgrounds();

}
