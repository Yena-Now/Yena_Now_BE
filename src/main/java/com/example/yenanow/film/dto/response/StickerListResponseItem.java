package com.example.yenanow.film.dto.response;

import com.example.yenanow.film.entity.Sticker;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class StickerListResponseItem {

    private String stickerUuid;
    private String stickerName;
    private String stickerUrl;

    public static StickerListResponseItem fromEntity(Sticker sticker) {
        return StickerListResponseItem.builder()
            .stickerUuid(sticker.getStickerUuid())
            .stickerName(sticker.getStickerName())
            .stickerUrl(sticker.getStickerUrl())
            .build();
    }
}
