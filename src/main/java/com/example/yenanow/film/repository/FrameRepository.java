package com.example.yenanow.film.repository;

import com.example.yenanow.film.entity.Frame;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrameRepository extends JpaRepository<Frame, String> {

}
