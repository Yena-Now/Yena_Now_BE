package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.dto.response.NcutDetailResponse;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.QNcut;
import com.example.yenanow.gallery.entity.Visibility;
import com.example.yenanow.users.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class NcutQueryRepositoryImpl implements NcutQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Ncut> findPublicGalleryWithUser(Pageable pageable) {
        QNcut ncut = QNcut.ncut;
        QUser user = QUser.user;

        List<Ncut> content = queryFactory
            .selectFrom(ncut)
            .join(ncut.user, user).fetchJoin()
            .where(ncut.visibility.eq(Visibility.PUBLIC))
            .orderBy(ncut.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(ncut.count())
            .from(ncut)
            .where(ncut.visibility.eq(Visibility.PUBLIC))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<Ncut> findFollowingsGalleryWithUser(List<String> userUuids,
        List<Visibility> visibilities, Pageable pageable) {
        QNcut ncut = QNcut.ncut;
        QUser user = QUser.user;

        List<Ncut> content = queryFactory
            .selectFrom(ncut)
            .join(ncut.user, user).fetchJoin()
            .where(ncut.user.userUuid.in(userUuids)
                .and(ncut.visibility.in(visibilities)))
            .orderBy(ncut.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long total = queryFactory
            .select(ncut.count())
            .from(ncut)
            .where(ncut.user.userUuid.in(userUuids)
                .and(ncut.visibility.in(visibilities)))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    @Transactional
    public void updateCommentCount(String ncutUuid, int commentCount) {
        QNcut ncut = QNcut.ncut;
        queryFactory.update(ncut)
            .set(ncut.commentCount, commentCount)
            .where(ncut.ncutUuid.eq(ncutUuid))
            .execute();
    }

    @Override
    public Optional<NcutDetailResponse> findNcutById(String ncutUuid) {
        QNcut ncut = QNcut.ncut;
        NcutDetailResponse ncutDetailResponse = queryFactory.select(
                Projections.fields(NcutDetailResponse.class,
                    ncut.ncutUrl,
                    ncut.user.userUuid,
                    ncut.user.nickname,
                    ncut.user.profileUrl,
                    ncut.content,
                    ncut.createdAt,
                    ncut.isRelay,
                    ncut.visibility
                )
            )
            .from(ncut)
            .where(ncut.ncutUuid.eq(ncutUuid))
            .fetchOne();
        
        return Optional.ofNullable(ncutDetailResponse);
    }
}
