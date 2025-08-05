package com.example.yenanow.film.dto.response;

import com.example.yenanow.film.entity.Background;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class BackgroundListResponse {

    private String backgroundUuid;
    private String backgroundName;
    private String backgroundUrl;

    public static BackgroundListResponse fromEntity(Background background) {
        return BackgroundListResponse.builder()
            .backgroundUuid(background.getBackgroundUuid())
            .backgroundName(background.getBackgroundName())
            .backgroundUrl(background.getBackgroundUrl())
            .build();
    }
}
