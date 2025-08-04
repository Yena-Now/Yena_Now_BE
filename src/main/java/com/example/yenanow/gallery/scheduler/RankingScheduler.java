package com.example.yenanow.gallery.scheduler;

import com.example.yenanow.gallery.entity.Visibility;
import com.example.yenanow.gallery.repository.NcutRankingQueryRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final int DAILY_TTL_HOURS = 25;  // 하루 + 여유 1시간
    private static final int WEEKLY_TTL_HOURS = 24 * 8; // 8일
    private final NcutRankingQueryRepository rankingRepository;
    private final StringRedisTemplate redis;

    /**
     * 매일 00:01(KST) 실행
     */
//    테스트용 ― 오늘 23:53에 한 번 실행
    @Scheduled(cron = "0 55 23 * * *", zone = "Asia/Seoul")
//    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
    public void cacheRankings() {
        LocalDate today = LocalDate.now(KST);

        LocalDateTime dailyStart = today.minusDays(1).atStartOfDay();
        LocalDateTime dailyEnd = today.atStartOfDay().minusNanos(1);
        LocalDateTime weeklyStart = today.minusDays(7).atStartOfDay();
        LocalDateTime weeklyEnd = dailyEnd;

        cache("ranking:daily_top10",
            rankingRepository.findRankingByPeriod(
                Visibility.PUBLIC, dailyStart, dailyEnd, 10),
            DAILY_TTL_HOURS);

        cache("ranking:weekly_top10",
            rankingRepository.findRankingByPeriod(
                Visibility.PUBLIC, weeklyStart, weeklyEnd, 10),
            WEEKLY_TTL_HOURS);
    }

    private void cache(String key, List<?> list, int ttlHours) {
        redis.delete(key); // 원자성을 위해 먼저 삭제
        list.forEach(obj -> {
            var ncut = (com.example.yenanow.gallery.entity.Ncut) obj;
            redis.opsForZSet().add(key, ncut.getNcutUuid(), ncut.getLikeCount());
        });
        redis.expire(key, Duration.ofHours(ttlHours));
        log.info("⏰  {} 캐시 완료 ({} 건)", key, list.size());
    }
}
