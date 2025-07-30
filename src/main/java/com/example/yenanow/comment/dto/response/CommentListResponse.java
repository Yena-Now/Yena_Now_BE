package com.example.yenanow.comment.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CommentListResponse {

    private int totalPage;
    private List<CommentResponse> comments;
}
