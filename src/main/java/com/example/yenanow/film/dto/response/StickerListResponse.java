package com.example.yenanow.film.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StickerListResponse {

    private int totalPages;
    private List<StickerListResponseItem> stickers;

}