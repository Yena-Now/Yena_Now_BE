package com.example.yenanow.gallery.service;

import com.example.yenanow.gallery.dto.response.NcutRankingResponse;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.Visibility;
import com.example.yenanow.gallery.repository.NcutRankingQueryRepository;
import com.example.yenanow.gallery.repository.NcutRepository;
import com.example.yenanow.s3.service.S3Service;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RankingServiceRedisImpl implements RankingServiceRedis {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int LIMIT = 10;
    private static final int DAILY_TTL_HOURS = 25;     // 하루 +1h
    private static final int WEEKLY_TTL_HOURS = 24 * 8; // 8일

    private final StringRedisTemplate redisTemplate;
    private final NcutRepository ncutRepository;
    private final NcutRankingQueryRepository rankingRepo; // Fallback 조회용
    private final S3Service s3Service;

    @Override
    public List<NcutRankingResponse> getDailyRanking() {

        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = today.minusDays(1).atStartOfDay(); // 어제 00:00
        LocalDateTime end = today.atStartOfDay().minusNanos(1); // 어제 23:59:59.999

        return fetch("ranking:daily_top10", start, end, DAILY_TTL_HOURS);
    }

    @Override
    public List<NcutRankingResponse> getWeeklyRanking() {

        LocalDate today = LocalDate.now(KST);
        LocalDateTime start = today.minusDays(7).atStartOfDay(); // 7일 전 00:00
        LocalDateTime end = today.atStartOfDay().minusNanos(1); // 어제 23:59:59.999

        return fetch("ranking:weekly_top10", start, end, WEEKLY_TTL_HOURS);
    }

    /**
     * 1) Redis 캐시 조회 → 없으면 2) DB Fallback + Redis 재적재
     */
    private List<NcutRankingResponse> fetch(String key,
        LocalDateTime start,
        LocalDateTime end,
        int ttlHours) {

        /* 1️⃣ Redis 캐시 시도 */
        Set<String> uuidSet = redisTemplate.opsForZSet()
            .reverseRange(key, 0, LIMIT - 1);

        if (uuidSet != null && !uuidSet.isEmpty()) {
            // 캐시 HIT ▶ 상세 DB 조회 후 반환
            return toResponse(sortedDetail(uuidSet));
        }

        /* 2️⃣ 캐시 MISS ▶ Fallback */
        log.warn("[Fallback] {} 캐시 비어 있음 → DB 재조회", key);

        List<Ncut> topN = rankingRepo.findRankingByPeriod(
            Visibility.PUBLIC, start, end, LIMIT);

        // Redis 재적재 (동기식: 간단, 비동기화도 가능)
        cacheToRedis(key, topN, ttlHours);

        return toResponse(topN);
    }

    /**
     * UUID 목록으로 상세 엔터티를 가져와 좋아요 순 재정렬
     */
    private List<Ncut> sortedDetail(Set<String> uuidSet) {

        List<Ncut> list = ncutRepository.findByNcutUuidIn(
            new ArrayList<>(uuidSet));

        return list.stream()
            .sorted(Comparator.comparingInt(Ncut::getLikeCount)
                .reversed())
            .collect(Collectors.toList());
    }

    /**
     * 엔터티 → DTO 변환
     */
    private List<NcutRankingResponse> toResponse(List<Ncut> list) {
        return list.stream()
            .map(n -> NcutRankingResponse.fromEntity(n, s3Service))
            .collect(Collectors.toList());
    }

    /**
     * Redis ZSET에 재적재 + TTL 설정
     */
    private void cacheToRedis(String key, List<Ncut> list, int ttlHours) {

        redisTemplate.delete(key);
        list.forEach(n ->
            redisTemplate.opsForZSet().add(key, n.getNcutUuid(), n.getLikeCount())
        );
        redisTemplate.expire(key, Duration.ofHours(ttlHours));

        log.info("⏰  {} 캐시 재적재 완료 ({} 건)", key, list.size());
    }
}
