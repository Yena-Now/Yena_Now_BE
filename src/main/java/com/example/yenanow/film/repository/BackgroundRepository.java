package com.example.yenanow.film.repository;

import com.example.yenanow.film.entity.Background;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BackgroundRepository extends JpaRepository<Background, String> {

}
