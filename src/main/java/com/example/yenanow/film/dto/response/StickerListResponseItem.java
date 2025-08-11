package com.example.yenanow.film.dto.response;

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

}