package com.example.yenanow.film.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FrameListResponseItem {

    private String frameUuid;
    private String frameName;
    private String frameUrl;
    private Integer frameCut;
    private Integer frameType;

}