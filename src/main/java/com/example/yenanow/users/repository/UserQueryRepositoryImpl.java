package com.example.yenanow.users.repository;

import com.example.yenanow.users.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
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

    public Optional<String> findNicknameById(String userUuid) {
        QUser user = QUser.user;

        String nickname = queryFactory
            .select(user.nickname)
            .from(user)
            .where(user.userUuid.eq(userUuid))
            .fetchOne();

        return Optional.ofNullable(nickname);
    }
}
