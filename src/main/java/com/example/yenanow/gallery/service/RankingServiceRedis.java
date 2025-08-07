package com.example.yenanow.gallery.service;

import com.example.yenanow.gallery.dto.response.NcutRankingResponse;
import java.util.List;

public interface RankingServiceRedis {

    /**
     * 어제(00:00:00~23:59:59 KST) Top-10
     */
    List<NcutRankingResponse> getDailyRanking();

    /**
     * 지난 7일(오늘 제외) Top-10
     */
    List<NcutRankingResponse> getWeeklyRanking();
}
