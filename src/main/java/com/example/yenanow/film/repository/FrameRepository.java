package com.example.yenanow.film.repository;

import com.example.yenanow.film.dto.response.FrameListResponseItem;
import com.example.yenanow.film.entity.Frame;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FrameRepository extends JpaRepository<Frame, String> {

    List<FrameListResponseItem> findByFrameCut(int frameCut);
}
