package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Relay;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RelayQueryRepository {

    Page<Relay> findByUserUuid(String userUuid, Pageable pageable);
}
