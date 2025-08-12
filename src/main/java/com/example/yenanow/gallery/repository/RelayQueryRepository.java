package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Relay;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RelayQueryRepository {

    Page<Relay> findByUserUuid(String userUuid, Pageable pageable);

    List<Relay> findExpiredRelaysWithCuts(LocalDateTime now);
}
