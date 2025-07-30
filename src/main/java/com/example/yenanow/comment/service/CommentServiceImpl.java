package com.example.yenanow.comment.service;

import com.example.yenanow.comment.dto.request.CommentCreateRequest;
import com.example.yenanow.comment.dto.request.CommentUpdateRequest;
import com.example.yenanow.comment.dto.response.CommentListResponse;
import com.example.yenanow.comment.dto.response.CommentResponse;
import com.example.yenanow.comment.entity.Comment;
import com.example.yenanow.comment.repository.CommentRepository;
import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.repository.NcutRepository;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final NcutRepository ncutRepository;
    private final UserRepository userRepository;

    @Override
    public CommentListResponse getComments(String ncutUuid, int pageNum, int display) {
        validateUuid(ncutUuid);

        Ncut ncut = ncutRepository.findById(ncutUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NCUT));

        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Comment> commentPage = commentRepository.findByNcut(ncut, pageable);

        if (commentPage.isEmpty()) {
            return CommentListResponse.builder()
                .totalPage(0)
                .comments(List.of())
                .build();
        }

        List<CommentResponse> comments = commentPage.stream()
            .map(c -> CommentResponse.builder()
                .commentUuid(c.getCommentUuid())
                .content(c.getContent())
                .userUuid(c.getUser().getUuid())
                .nickname(c.getUser().getNickname())
                .profileUrl(c.getUser().getProfileUrl())
                .createdAt(c.getCreatedAt())
                .build())
            .collect(Collectors.toList());

        return CommentListResponse.builder()
            .totalPage(commentPage.getTotalPages())
            .comments(comments)
            .build();
    }

    @Override
    @Transactional
    public CommentListResponse addComment(String ncutUuid, String userUuid,
        CommentCreateRequest request, int pageNum, int display) {
        validateUuid(ncutUuid);
        validateUuid(userUuid);

        Ncut ncut = ncutRepository.findById(ncutUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NCUT));
        User user = userRepository.findById(userUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        commentRepository.save(
            Comment.builder()
                .content(request.getContent())
                .ncut(ncut)
                .user(user)
                .build()
        );

        return getComments(ncutUuid, pageNum, display);
    }

    @Override
    @Transactional
    public void updateComment(String commentUuid, CommentUpdateRequest request, String userUuid) {
        validateUuid(commentUuid);
        validateUuid(userUuid);

        Comment comment = commentRepository.findById(commentUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));

        if (!comment.getUser().getUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_COMMENT);
        }
        comment.setContent(request.getContent());
    }

    @Override
    @Transactional
    public void deleteComment(String commentUuid, String userUuid) {
        validateUuid(commentUuid);
        validateUuid(userUuid);

        Comment comment = commentRepository.findById(commentUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));

        if (!comment.getUser().getUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.FORBIDDEN_COMMENT);
        }
        commentRepository.delete(comment);
    }

    private void validateUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }
}
