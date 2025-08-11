package com.example.yenanow.film.controller;

import com.example.yenanow.film.dto.request.MergeRequest;
import com.example.yenanow.film.dto.response.MergeResponse;
import com.example.yenanow.film.service.FilmService;
import io.swagger.v3.oas.annotations.Operation;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/film")
public class FfmpegController {

    private final FilmService filmService;

    @Operation(summary = "결과물 합성 요청", description = "촬영한 컷들을 N컷으로 합성 요청합니다.")
    @PostMapping("/merge")
    public CompletableFuture<MergeResponse> createMergedOutput(
        @AuthenticationPrincipal Object principal,
        @RequestBody MergeRequest mergeRequest) {
        String userUuid = principal.toString();
        return filmService.createMergedOutput(mergeRequest, userUuid);
    }
}