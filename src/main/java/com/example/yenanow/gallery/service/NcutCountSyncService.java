package com.example.yenanow.gallery.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.repository.NcutRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@EnableAsync
public class NcutCountSyncService {

    private final NcutRepository ncutRepository;

    @Async
    @Transactional
    public void syncLikeCountToDB(String ncutUuid, Integer likeCount) {
        Ncut ncut = ncutRepository.findById(ncutUuid).orElseThrow(() -> new BusinessException(
            ErrorCode.NOT_FOUND_NCUT));
        ncut.setLikeCount(likeCount);
    }
}
