package com.example.yenanow.users.repository;

import com.example.yenanow.users.entity.QFollow;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FollowQueryRepositoryImpl implements FollowQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<String> findFollowingUuids(String userUuid) {
        QFollow follow = QFollow.follow;

        return queryFactory
            .select(follow.toUser.uuid)  // toUser가 String UUID일 경우
            .from(follow)
            .where(follow.fromUser.uuid.eq(userUuid))
            .fetch();
    }
}
