package com.example.yenanow.gallery.repository;

import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.entity.Visibility;
import java.time.LocalDateTime;
import java.util.List;

public interface NcutRankingQueryRepository {

    List<Ncut> findRankingByPeriod(
        Visibility visibility,
        LocalDateTime start,
        LocalDateTime end,
        int limit
    );
}
