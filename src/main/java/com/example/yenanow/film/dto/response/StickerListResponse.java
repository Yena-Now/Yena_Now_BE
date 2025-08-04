package com.example.yenanow.film.dto.response;

import com.example.yenanow.film.entity.Sticker;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
@Builder
public class StickerListResponse {

    private int totalPages;
    private List<StickerListResponseItem> stickers;

    public static StickerListResponse fromEntity(Page<Sticker> page) {
        List<StickerListResponseItem> items = page.getContent().stream()
            .map(StickerListResponseItem::fromEntity)
            .toList();
        return new StickerListResponse(page.getTotalPages(), items);
    }
}
