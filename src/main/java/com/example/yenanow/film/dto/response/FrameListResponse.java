package com.example.yenanow.film.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FrameListResponse {
    private String frameUuid;
    private String frameName;
    private String frameUrl;
}
