package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Relay;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelayRepository extends JpaRepository<Relay, String>, RelayQueryRepository {

    boolean existsByRelayUuidAndUserUserUuid(String relayUuid, String userUuid);
}
