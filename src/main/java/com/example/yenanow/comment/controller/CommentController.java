package com.example.yenanow.comment.controller;

import com.example.yenanow.comment.dto.request.CommentCreateRequest;
import com.example.yenanow.comment.dto.request.CommentUpdateRequest;
import com.example.yenanow.comment.dto.response.CommentListResponse;
import com.example.yenanow.comment.service.CommentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Comment", description = "N컷 댓글 관련 API")
@RestController
@RequestMapping("/api/ncuts/{ncutUuid}/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 목록 조회", description = "특정 N컷에 작성된 댓글 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<CommentListResponse> getComments(
        @Parameter(description = "댓글을 조회할 N컷 UUID", required = true)
        @PathVariable String ncutUuid,
        @Parameter(description = "페이지 번호 (기본값: 0)")
        @RequestParam(defaultValue = "0") int pageNum,
        @Parameter(description = "페이지당 개수 (기본값: 30)")
        @RequestParam(defaultValue = "30") int display) {

        return ResponseEntity.ok(commentService.getComments(ncutUuid, pageNum, display));
    }

    @Operation(summary = "댓글 작성", description = "특정 N컷에 새로운 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<CommentListResponse> addComment(
        @Parameter(description = "댓글을 작성할 N컷 UUID", required = true)
        @PathVariable String ncutUuid,
        @Parameter(hidden = true)
        @AuthenticationPrincipal(expression = "userUuid") String userUuid,
        @RequestBody CommentCreateRequest request,
        @Parameter(description = "페이지 번호 (기본값: 0)")
        @RequestParam(defaultValue = "0") int pageNum,
        @Parameter(description = "페이지당 개수 (기본값: 30)")
        @RequestParam(defaultValue = "30") int display) {

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(commentService.addComment(ncutUuid, userUuid, request, pageNum, display));
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글 내용을 수정합니다.")
    @PutMapping("/{commentUuid}")
    public ResponseEntity<Void> updateComment(
        @Parameter(description = "댓글이 속한 N컷 UUID", required = true)
        @PathVariable String ncutUuid,
        @Parameter(description = "수정할 댓글 UUID", required = true)
        @PathVariable String commentUuid,
        @Parameter(hidden = true)
        @AuthenticationPrincipal(expression = "userUuid") String userUuid,
        @RequestBody CommentUpdateRequest request) {

        commentService.updateComment(commentUuid, request, userUuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "댓글 삭제", description = "본인이 작성한 댓글을 삭제합니다.")
    @DeleteMapping("/{commentUuid}")
    public ResponseEntity<Void> deleteComment(
        @Parameter(description = "댓글이 속한 N컷 UUID", required = true)
        @PathVariable String ncutUuid,
        @Parameter(description = "삭제할 댓글 UUID", required = true)
        @PathVariable String commentUuid,
        @Parameter(hidden = true)
        @AuthenticationPrincipal(expression = "userUuid") String userUuid) {

        commentService.deleteComment(commentUuid, userUuid);
        return ResponseEntity.noContent().build();
    }
}
