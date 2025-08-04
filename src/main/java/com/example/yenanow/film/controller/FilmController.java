package com.example.yenanow.film.controller;

import com.example.yenanow.film.dto.response.BackgroundListResponse;
import com.example.yenanow.film.dto.response.FrameListResponse;
import com.example.yenanow.film.dto.response.StickerListResponse;
import com.example.yenanow.film.service.FilmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Film", description = "N컷 촬영 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/film")
public class FilmController {

    private final FilmService filmService;

    @Operation(summary = "프레임 목록 조회", description = "필름에 사용 가능한 모든 프레임 목록을 조회합니다.")
    @GetMapping("/frames")
    public List<FrameListResponse> getFrames() {
        return filmService.getFrames();
    }

    @Operation(summary = "스티커 목록 조회", description = "필름에 사용 가능한 스티커 목록을 페이지네이션으로 조회합니다.")
    @GetMapping("/stickers")
    public StickerListResponse getStickers(
        @Parameter(description = "페이지 번호 (기본값: 0)", example = "0")
        @RequestParam(defaultValue = "0") int pageNum,
        @Parameter(description = "페이지당 표시할 개수 (기본값: 30)", example = "30")
        @RequestParam(defaultValue = "30") int display) {

        Pageable pageable = PageRequest.of(pageNum, display);
        return filmService.getStickers(pageable);
    }

    @Operation(summary = "배경 목록 조회", description = "필름에 사용 가능한 모든 배경 목록을 조회합니다.")
    @GetMapping("/backgrounds")
    public List<BackgroundListResponse> getBackgrounds() {
        return filmService.getBackgrounds();
    }
}
