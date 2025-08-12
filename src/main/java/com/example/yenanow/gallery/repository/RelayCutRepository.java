package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.RelayCut;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RelayCutRepository extends JpaRepository<RelayCut, String> {

    List<RelayCut> findByRelayRelayUuid(String relayUuid);
}
