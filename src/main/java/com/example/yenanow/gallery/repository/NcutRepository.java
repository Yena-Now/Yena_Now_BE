package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Ncut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NcutRepository extends JpaRepository<Ncut, String> {

    Page<Ncut> findByUserUuidOrderByCreatedAtDesc(String userUuid, Pageable pageable);
}