package com.example.yenanow.film.repository;

import com.example.yenanow.film.entity.Sticker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StickerRepository extends JpaRepository<Sticker, String> {

}
