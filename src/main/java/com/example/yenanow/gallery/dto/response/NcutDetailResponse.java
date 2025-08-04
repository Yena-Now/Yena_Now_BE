package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.Visibility;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NcutDetailResponse {

    private String ncutUuid;
    private String ncutUrl;
    private String userUuid;
    private String nickname;
    private String profileUrl;
    private String content;
    private LocalDateTime createdAt;
    private Integer likeCount;
    private Integer commentCount;
    private Boolean isRelay;
    private Visibility visibility;
    private Boolean isMine;
}
