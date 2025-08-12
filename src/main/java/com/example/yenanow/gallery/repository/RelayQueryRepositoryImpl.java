package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.QRelay;
import com.example.yenanow.gallery.entity.QRelayParticipant;
import com.example.yenanow.gallery.entity.Relay;
import com.example.yenanow.users.entity.QUser;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public class RelayQueryRepositoryImpl implements RelayQueryRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Relay> findByUserUuid(String userUuid, Pageable pageable) {
        QRelay relay = QRelay.relay;
        QRelayParticipant participant = QRelayParticipant.relayParticipant;
        QUser user = QUser.user;

        List<String> relayUuids = queryFactory
            .select(relay.relayUuid)
            .from(relay)
            .where(relay.relayUuid.in(
                JPAExpressions
                    .select(participant.relay.relayUuid)
                    .from(participant)
                    .where(participant.user.userUuid.eq(userUuid))
            ))
            .orderBy(relay.createdAt.desc())
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        if (relayUuids.isEmpty()) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }

        List<Relay> content = queryFactory
            .selectFrom(relay).distinct()
            .join(relay.participants, participant).fetchJoin()
            .join(participant.user, user).fetchJoin()
            .where(relay.relayUuid.in(relayUuids))
            .orderBy(relay.createdAt.desc())
            .fetch();

        Long total = queryFactory
            .select(relay.countDistinct())
            .from(relay)
            .join(relay.participants, participant)
            .where(participant.user.userUuid.eq(userUuid))
            .fetchOne();
        long totalCount = (total == null) ? 0L : total;

        return new PageImpl<>(content, pageable, totalCount);
    }

    @Override
    public List<Relay> findExpiredRelaysWithCuts(LocalDateTime now) {
        QRelay relay = QRelay.relay;

        return queryFactory
            .selectFrom(relay).distinct()
            .leftJoin(relay.cuts).fetchJoin()
            .where(relay.expiredAt.before(now))
            .fetch();
    }
}
