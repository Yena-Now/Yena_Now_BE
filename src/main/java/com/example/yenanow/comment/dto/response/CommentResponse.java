package com.example.yenanow.comment.dto.response;

import com.example.yenanow.comment.entity.Comment;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class CommentResponse {

    private String commentUuid;
    private String comment;
    private String userUuid;
    private String nickname;
    private String profileUrl;
    private LocalDateTime createdAt;

    /**
     * Entity -> DTO 변환
     */
    public static CommentResponse fromEntity(Comment entity) {
        return CommentResponse.builder()
            .commentUuid(entity.getCommentUuid())
            .comment(entity.getContent())
            .userUuid(entity.getUser().getUuid())
            .nickname(entity.getUser().getNickname())
            .profileUrl(entity.getUser().getProfileUrl())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}
