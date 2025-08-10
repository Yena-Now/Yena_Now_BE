package com.example.yenanow.film.dto.response;

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

}