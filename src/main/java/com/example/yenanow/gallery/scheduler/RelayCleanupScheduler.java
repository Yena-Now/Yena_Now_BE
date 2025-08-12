package com.example.yenanow.gallery.scheduler;

import com.example.yenanow.gallery.entity.Relay;
import com.example.yenanow.gallery.entity.RelayCut;
import com.example.yenanow.gallery.repository.RelayRepository;
import com.example.yenanow.s3.service.S3Service;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class RelayCleanupScheduler {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private final RelayRepository relayRepository;
    private final S3Service s3Service;

    @Scheduled(cron = "0 1 0 * * *", zone = "Asia/Seoul")
    @Transactional
    public void deleteExpiredRelays() {
        LocalDateTime now = LocalDateTime.now(KST);
        List<Relay> expiredRelays = relayRepository.findExpiredRelaysWithCuts(now);

        if (expiredRelays.isEmpty()) {
            return;
        }

        List<String> s3KeysToDelete = expiredRelays.stream()
            .flatMap(relay -> relay.getCuts().stream())
            .map(RelayCut::getCutUrl)
            .filter(key -> key != null && !key.isBlank())
            .toList();

        if (!s3KeysToDelete.isEmpty()) {
            s3KeysToDelete.forEach(s3Service::deleteObject);
            log.info("☁️ S3에서 {}개의 파일을 삭제했습니다.", s3KeysToDelete.size());
        }

        relayRepository.deleteAllInBatch(expiredRelays);
        log.info("🗑️ 총 {}개의 만료된 이어찍기를 삭제했습니다.", expiredRelays.size());
    }
}