package com.example.yenanow.comment.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentResponse {

    private String commentUuid;
    private String content;
    private String userUuid;
    private String nickname;
    private String profileUrl;
    private LocalDateTime createdAt;
}
