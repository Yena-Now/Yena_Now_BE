package com.example.yenanow.gallery.controller;

import com.example.yenanow.gallery.dto.response.NcutRankingResponse;
import com.example.yenanow.gallery.service.RankingServiceRedis;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Ranking", description = "N컷 랭킹 API")
@RestController
@RequestMapping("/gallery/ranking")
@RequiredArgsConstructor
public class RankingController {

    private final RankingServiceRedis rankingServiceRedis;

    @Operation(summary = "어제의 순간 목록 조회")
    @GetMapping("/daily")
    public ResponseEntity<List<NcutRankingResponse>> getDailyRanking() {
        return ResponseEntity.ok(rankingServiceRedis.getDailyRanking());
    }

    @Operation(summary = "지난주의 순간 목록 조회")
    @GetMapping("/weekly")
    public ResponseEntity<List<NcutRankingResponse>> getWeeklyRanking() {
        return ResponseEntity.ok(rankingServiceRedis.getWeeklyRanking());
    }
}