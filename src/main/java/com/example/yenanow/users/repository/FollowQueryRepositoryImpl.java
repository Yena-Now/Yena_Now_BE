package com.example.yenanow.users.repository;

import com.example.yenanow.users.entity.QFollow;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class FollowQueryRepositoryImpl implements FollowQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<String> findFollowingUuids(String userUuid) {
        QFollow follow = QFollow.follow;

        return queryFactory
            .select(follow.toUser)  // toUser가 String UUID일 경우
            .from(follow)
            .where(follow.fromUser.eq(userUuid))
            .fetch();
    }
}
