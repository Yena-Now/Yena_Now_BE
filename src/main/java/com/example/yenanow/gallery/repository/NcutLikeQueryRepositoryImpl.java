package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.dto.response.NcutLikesResponseItem;
import com.example.yenanow.gallery.entity.QNcut;
import com.example.yenanow.gallery.entity.QNcutLike;
import com.example.yenanow.users.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class NcutLikeQueryRepositoryImpl implements NcutLikeQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<Page<NcutLikesResponseItem>> findNcutLikeByNcutUuid(String ncutUuid,
        Pageable pageable) {
        QNcutLike ncutLike = QNcutLike.ncutLike;
        QNcut ncut = QNcut.ncut;
        QUser user = QUser.user;

        Integer total = queryFactory
            .select(ncut.likeCount)
            .from(ncut)
            .where(ncut.ncutUuid.eq(ncutUuid))
            .fetchOne();

        if (total == null) {
            return Optional.empty();
        }

        if (total == 0) {
            return Optional.of(new PageImpl<>(Collections.emptyList(), pageable, 0));
        }

        List<NcutLikesResponseItem> ncutLikesResponseItems = queryFactory
            .select(
                Projections.fields(NcutLikesResponseItem.class,
                    ncutLike.user.userUuid,
                    ncutLike.user.name,
                    ncutLike.user.nickname,
                    ncutLike.user.profileUrl
                )
            )
            .from(ncutLike)
            .join(ncutLike.user, user)
            .where(ncutLike.ncut.ncutUuid.eq(ncutUuid))
            .orderBy(ncutLike.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        return Optional.of(new PageImpl<>(ncutLikesResponseItems, pageable, total));
    }
}
