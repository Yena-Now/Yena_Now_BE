package com.example.yenanow.gallery.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NcutLikeResponse {

    private Boolean isLiked;
    private Integer likeCount;
}
