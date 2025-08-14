package com.example.yenanow.comment.dto.response;

import com.example.yenanow.comment.dto.query.CommentQueryDto;
import com.example.yenanow.comment.entity.Comment;
import com.example.yenanow.s3.service.S3Service;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@Builder
public class CommentListResponse {

    private int totalPage;
    private List<CommentResponse> comments;

    /**
     * Page<Comment> -> CommentListResponse 변환
     */
    public static CommentListResponse fromEntity(Page<Comment> commentPage, S3Service s3Service) {
        return CommentListResponse.builder()
            .totalPage(commentPage.getTotalPages())
            .comments(commentPage.getContent()
                .stream()
                .map(c -> CommentResponse.fromEntity(c, s3Service))
                .collect(Collectors.toList()))
            .build();
    }

    public static CommentListResponse fromQueryDtoPage(Page<CommentQueryDto> dtoPage,
        S3Service s3Service) {
        return CommentListResponse.builder()
            .totalPage(dtoPage.getTotalPages())
            .comments(dtoPage.getContent()
                .stream()
                .map(dto -> CommentResponse.fromQueryDto(dto, s3Service))
                .collect(Collectors.toList()))
            .build();
    }
}