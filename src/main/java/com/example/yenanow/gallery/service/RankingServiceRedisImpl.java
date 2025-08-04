package com.example.yenanow.gallery.service;

import com.example.yenanow.gallery.dto.response.NcutRankingResponse;
import com.example.yenanow.gallery.repository.NcutRepository;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingServiceRedisImpl implements RankingServiceRedis {

    private static final int LIMIT = 10;
    private final StringRedisTemplate redis;
    private final NcutRepository ncutRepository;

    @Override
    public List<NcutRankingResponse> getDailyRanking() {
        return fetch("ranking:daily_top10");
    }

    @Override
    public List<NcutRankingResponse> getWeeklyRanking() {
        return fetch("ranking:weekly_top10");
    }

    /* ---------------- private --------------- */

    private List<NcutRankingResponse> fetch(String key) {

        // 키가 없으면 아직 배치 전 or 실패 – 필요 시 Fallback 쿼리 호출
        if (!redis.hasKey(key)) {
            throw new IllegalStateException("랭킹 갱신 중입니다. 잠시 후 다시 시도해 주세요.");
        }

        // 1) UUID 리스트 → 좋아요 순으로 역정렬
        List<String> uuids = Optional.ofNullable(
                redis.opsForZSet().reverseRange(key, 0, LIMIT - 1))
            .orElse(Collections.emptySet())
            .stream().toList();

        if (uuids.isEmpty()) {
            return List.of();
        }

        // 2) 상세 정보 조회 (썸네일 URL 포함)
        return ncutRepository.findByNcutUuidIn(uuids).stream()
            .map(NcutRankingResponse::fromEntity)
            // Redis ZSET 점수순이지만 DB 찾아오면 순서 깨질 수 있어 다시 정렬
            .sorted(Comparator.comparingInt(NcutRankingResponse::getLikeCount)
                .reversed())
            .toList();
    }
}
