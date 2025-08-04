package com.example.yenanow.film.controller;

import com.example.yenanow.film.dto.response.BackgroundListResponse;
import com.example.yenanow.film.dto.response.FrameListResponse;
import com.example.yenanow.film.dto.response.StickerListResponse;
import com.example.yenanow.film.service.FilmService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/film")
public class FilmController {

    private final FilmService filmService;

    @GetMapping("/frames")
    public List<FrameListResponse> getFrames() {
        return filmService.getFrames();
    }

    @GetMapping("/stickers")
    public StickerListResponse getStickers(
        @RequestParam(defaultValue = "0") int pageNum,
        @RequestParam(defaultValue = "30") int display) {

        Pageable pageable = PageRequest.of(pageNum, display);
        return filmService.getStickers(pageable);
    }

    @GetMapping(value = "/backgrounds")
    public List<BackgroundListResponse> getBackgrounds() {
        return filmService.getBackgrounds();
    }
}
