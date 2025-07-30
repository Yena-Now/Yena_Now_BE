package com.example.yenanow.comment.service;

import com.example.yenanow.comment.dto.request.CommentCreateRequest;
import com.example.yenanow.comment.dto.request.CommentUpdateRequest;
import com.example.yenanow.comment.dto.response.CommentListResponse;

public interface CommentService {

    CommentListResponse getComments(String ncutUuid, int pageNum, int display);

    CommentListResponse addComment(String ncutUuid, String userUuid, CommentCreateRequest request,
        int pageNum, int display);

    void updateComment(String commentUuid, CommentUpdateRequest request, String userUuid);

    void deleteComment(String commentUuid, String userUuid);
}