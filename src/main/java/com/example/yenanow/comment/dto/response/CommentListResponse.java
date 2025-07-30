package com.example.yenanow.comment.dto.response;

import com.example.yenanow.comment.entity.Comment;
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
    public static CommentListResponse fromEntity(Page<Comment> commentPage) {
        return CommentListResponse.builder()
            .totalPage(commentPage.getTotalPages())
            .comments(commentPage.getContent()
                .stream()
                .map(CommentResponse::fromEntity)
                .collect(Collectors.toList()))
            .build();
    }
}
