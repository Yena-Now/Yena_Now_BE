package com.example.yenanow.users.repository;

import com.example.yenanow.users.dto.response.UserInviteSearchResponseItem;
import com.example.yenanow.users.dto.response.UserSearchResponseItem;
import com.example.yenanow.users.entity.QFollow;
import com.example.yenanow.users.entity.QUser;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    @Transactional
    public void updateFollowCount(String userUuid, int followerCount, int followingCount) {
        QUser user = QUser.user;

        queryFactory.update(user)
            .set(user.followerCount, followerCount)
            .set(user.followingCount, followingCount)
            .where(user.userUuid.eq(userUuid))
            .execute();
    }

    @Override
    public Optional<String> findNicknameById(String userUuid) {
        QUser user = QUser.user;

        String nickname = queryFactory
            .select(user.nickname)
            .from(user)
            .where(user.userUuid.eq(userUuid))
            .fetchOne();

        return Optional.ofNullable(nickname);
    }

    @Override
    public Page<UserSearchResponseItem> findUsersByKeyword(String currentUserUuid,
        String keyword, Pageable pageable) {
        QUser user = QUser.user;
        QFollow follow = QFollow.follow;

        // isFollowing 서브쿼리로 처리
        BooleanExpression isFollowing = JPAExpressions
            .selectOne()
            .from(follow)
            .where(follow.fromUser.userUuid.eq(currentUserUuid)
                .and(follow.toUser.userUuid.eq(user.userUuid)))
            .exists();

        List<UserSearchResponseItem> content = queryFactory
            .select(Projections.constructor(
                UserSearchResponseItem.class,
                user.userUuid,
                user.profileUrl,
                user.name,
                user.nickname,
                isFollowing
            ))
            .from(user)
            .where(
                user.name.like("%" + keyword + "%")
                    .or(user.nickname.like("%" + keyword + "%")),
                user.userUuid.ne(currentUserUuid) // 자기 자신은 겸색결과에서 제외
            )
            .fetch();

        Long total = queryFactory
            .select(user.count())
            .from(user)
            .where(user.name.like("%" + keyword + "%")
                .or(user.nickname.like("%" + keyword + "%")), user.userUuid.ne(currentUserUuid))
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    @Override
    public Page<UserInviteSearchResponseItem> findFollowersByKeyword(
        String currentUserUuid, String keyword, Pageable pageable) {

        QUser user = QUser.user;
        QFollow follow = QFollow.follow;

        BooleanExpression matchesKeyword =
            user.name.containsIgnoreCase(keyword)
                .or(user.nickname.containsIgnoreCase(keyword));

        List<UserInviteSearchResponseItem> content = queryFactory
            .select(Projections.constructor(
                UserInviteSearchResponseItem.class,
                user.userUuid,
                user.profileUrl,
                user.name,
                user.nickname
            ))
            .from(user)
            .join(follow).on(
                follow.fromUser.userUuid.eq(user.userUuid),
                follow.toUser.userUuid.eq(currentUserUuid)
            )
            .where(matchesKeyword)
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        Long total = queryFactory
            .select(user.countDistinct())
            .from(user)
            .join(follow).on(
                follow.fromUser.userUuid.eq(user.userUuid),
                follow.toUser.userUuid.eq(currentUserUuid)
            )
            .where(matchesKeyword)
            .fetchOne();

        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

}