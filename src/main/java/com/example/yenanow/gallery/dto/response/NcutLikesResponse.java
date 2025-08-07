package com.example.yenanow.gallery.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NcutLikesResponse {

    private Boolean isLiked;
    private Integer likeCount;
    private List<NcutLikesResponseItem> likes;
}
