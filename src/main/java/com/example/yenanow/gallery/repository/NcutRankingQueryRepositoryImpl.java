package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.QNcut;
import com.example.yenanow.gallery.entity.Visibility;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NcutRankingQueryRepositoryImpl implements NcutRankingQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Ncut> findRankingByPeriod(Visibility visibility,
        LocalDateTime start,
        LocalDateTime end,
        int limit) {

        QNcut ncut = QNcut.ncut;

        return queryFactory
            .selectFrom(ncut)
            .where(
                ncut.visibility.eq(visibility),
                ncut.createdAt.between(start, end)
            )
            .orderBy(ncut.likeCount.desc())
            .limit(limit)
            .fetch();
    }
}