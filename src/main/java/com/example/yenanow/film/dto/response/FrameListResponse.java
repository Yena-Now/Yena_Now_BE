package com.example.yenanow.film.dto.response;

import com.example.yenanow.film.entity.Frame;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Builder
public class FrameListResponse {

    private String frameUuid;
    private String frameName;
    private String frameUrl;

    public static FrameListResponse fromEntity(Frame entity) {
        return FrameListResponse.builder()
            .frameUuid(entity.getFrameUuid())
            .frameName(entity.getFrameName())
            .frameUrl(entity.getFrameUrl())
            .build();
    }
}
