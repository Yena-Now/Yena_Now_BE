package com.example.yenanow.users.repository;

import com.example.yenanow.users.entity.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserQueryRepositoryImpl implements UserQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<String> findNicknameById(String userUuid) {
        QUser user = QUser.user;

        String nickname = queryFactory
            .select(user.nickname)
            .from(user)
            .where(user.uuid.eq(userUuid))
            .fetchOne();

        return Optional.ofNullable(nickname);
    }
}
