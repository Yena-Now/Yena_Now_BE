package com.example.yenanow.comment.dto.response;

import com.example.yenanow.comment.dto.query.CommentQueryDto;
import com.example.yenanow.comment.entity.Comment;
import com.example.yenanow.s3.service.S3Service;
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

    public static CommentResponse fromEntity(Comment entity, S3Service s3Service) {
        return CommentResponse.builder()
            .commentUuid(entity.getCommentUuid())
            .comment(entity.getContent())
            .userUuid(entity.getUser().getUserUuid())
            .nickname(entity.getUser().getNickname())
            .profileUrl(s3Service.getFileUrl(entity.getUser().getProfileUrl()))
            .createdAt(entity.getCreatedAt())
            .build();
    }

    public static CommentResponse fromQueryDto(CommentQueryDto dto, S3Service s3Service) {
        String userUuid = dto.getUserUuid();
        String name = dto.getName();
        String nickname = dto.getNickname();
        String profileUrl = null;

        if (dto.getDeletedAt() != null) {
            userUuid = null;
            name = null;
            nickname = "탈퇴한 사용자";
        } else if (dto.getProfileUrl() != null) {
            profileUrl = s3Service.getFileUrl(dto.getProfileUrl());
        }

        return CommentResponse.builder()
            .commentUuid(dto.getCommentUuid())
            .comment(dto.getContent())
            .userUuid(dto.getUserUuid())
            .nickname(nickname)
            .profileUrl(profileUrl)
            .createdAt(dto.getCreatedAt())
            .build();
    }
}