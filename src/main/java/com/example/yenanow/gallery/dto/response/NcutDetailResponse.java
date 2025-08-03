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

    String ncutUuid;
    String ncutUrl;
    String userUuid;
    String nickname;
    String profileUrl;
    String content;
    LocalDateTime createdAt;
    Integer likeCount;
    Integer commentCount;
    Boolean isRelay;
    Visibility visibility;
    Boolean isMine;
}
