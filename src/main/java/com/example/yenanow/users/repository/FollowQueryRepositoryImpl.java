package com.example.yenanow.users.repository;

import com.example.yenanow.users.dto.response.FollowerResponseItem;
import com.example.yenanow.users.dto.response.FollowingResponseItem;
import com.example.yenanow.users.entity.QFollow;
import com.example.yenanow.users.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FollowQueryRepositoryImpl implements FollowQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<String> findFollowingUuids(String userUuid) {
        QFollow follow = QFollow.follow;

        return queryFactory
            .select(follow.toUser.userUuid)  // toUser가 String UUID일 경우
            .from(follow)
            .where(follow.fromUser.userUuid.eq(userUuid))
            .fetch();
    }

    @Override
    public Page<FollowingResponseItem> findFollowings(
        String targetUserUuid,
        String currentUserUuid,
        Pageable pageable) {
        QFollow follow = QFollow.follow;
        QFollow followByMe = new QFollow("followByMe"); // alias
        QUser toUser = QUser.user;

        // isFollowing 서브쿼리로 처리
        BooleanExpression isFollowing = JPAExpressions
            .selectOne()
            .from(followByMe)
            .where(
                followByMe.fromUser.userUuid.eq(currentUserUuid),
                followByMe.toUser.userUuid.eq(toUser.userUuid)
            )
            .exists();

        List<FollowingResponseItem> content = queryFactory
            .select(Projections.constructor( // Projection: Entity에서 생성자 기반으로 일부 필드만 선택해 dto로 매핑
                FollowingResponseItem.class,
                toUser.userUuid,
                toUser.name,
                toUser.nickname,
                toUser.profileUrl,
                isFollowing
            ))
            .from(follow)
            .join(follow.toUser, toUser)
            .where(follow.fromUser.userUuid.eq(targetUserUuid))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(follow.followUuid.desc())
            .fetch();

        long total = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.fromUser.userUuid.eq(targetUserUuid))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public Page<FollowerResponseItem> findFollowers(
        String targetUserUuid,
        String currentUserUuid,
        Pageable pageable) {
        QFollow follow = QFollow.follow;
        QFollow followByMe = new QFollow("followByMe"); // alias
        QUser fromUser = QUser.user;

        // isFollowing 서브쿼리로 처리
        BooleanExpression isFollowing = JPAExpressions
            .selectOne()
            .from(followByMe)
            .where(
                followByMe.fromUser.userUuid.eq(currentUserUuid),
                followByMe.toUser.userUuid.eq(fromUser.userUuid)
            )
            .exists();

        List<FollowerResponseItem> content = queryFactory
            .select(Projections.constructor(
                FollowerResponseItem.class,
                fromUser.userUuid,
                fromUser.name,
                fromUser.nickname,
                fromUser.profileUrl,
                isFollowing
            ))
            .from(follow)
            .join(follow.fromUser, fromUser)
            .where(follow.toUser.userUuid.eq(targetUserUuid))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(follow.followUuid.desc())
            .fetch();

        long total = queryFactory
            .select(follow.count())
            .from(follow)
            .where(follow.toUser.userUuid.eq(targetUserUuid))
            .fetchOne();

        return new PageImpl<>(content, pageable, total);
    }
}
