package com.example.yenanow.comment.service;

import com.example.yenanow.gallery.repository.NcutRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommentCountSyncService {

    private final StringRedisTemplate redisTemplate;
    private final NcutRepository ncutRepository;

    /**
     * Redis 댓글 수를 비동기로 DB에 백업
     */
    @Async
    public void syncCommentCountToDB(String ncutUuid) {
        Object countValue = redisTemplate.opsForHash().get("ncut:" + ncutUuid, "comment_count");
        if (countValue != null) {
            int commentCount = Integer.parseInt(countValue.toString());
            ncutRepository.updateCommentCount(ncutUuid, commentCount);
        }
    }
}
