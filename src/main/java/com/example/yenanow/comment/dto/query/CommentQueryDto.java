package com.example.yenanow.comment.dto.query;

import java.time.LocalDateTime;

public interface CommentQueryDto {

    String getCommentUuid();

    String getContent();

    LocalDateTime getCreatedAt();

    String getUserUuid();

    String getName();

    String getNickname();

    String getProfileUrl();

    LocalDateTime getDeletedAt();
}