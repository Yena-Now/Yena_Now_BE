package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.QNcut;
import com.example.yenanow.gallery.entity.Visibility;
import com.example.yenanow.users.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
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
        .where(ncut.user.uuid.in(userUuids)
            .and(ncut.visibility.in(visibilities)))
        .orderBy(ncut.createdAt.desc())
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetch();

    long total = queryFactory
        .select(ncut.count())
        .from(ncut)
        .where(ncut.user.uuid.in(userUuids)
            .and(ncut.visibility.in(visibilities)))
        .fetchOne();

    return new PageImpl<>(content, pageable, total);
  }
}
