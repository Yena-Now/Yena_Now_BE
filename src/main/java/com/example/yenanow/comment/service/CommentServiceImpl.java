package com.example.yenanow.comment.service;

import com.example.yenanow.comment.dto.request.CommentCreateRequest;
import com.example.yenanow.comment.dto.request.CommentUpdateRequest;
import com.example.yenanow.comment.dto.response.CommentListResponse;
import com.example.yenanow.comment.entity.Comment;
import com.example.yenanow.comment.repository.CommentRepository;
import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.gallery.repository.NcutRepository;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final NcutRepository ncutRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public CommentListResponse getComments(String ncutUuid, int pageNum, int display) {
        Pageable pageable = PageRequest.of(pageNum, display);
        Page<Comment> commentPage = commentRepository.findByNcutNcutUuid(ncutUuid, pageable);
        return CommentListResponse.fromEntity(commentPage);
    }

    @Override
    public CommentListResponse addComment(String ncutUuid, String userUuid,
        CommentCreateRequest request, int pageNum, int display) {
        Ncut ncut = ncutRepository.findById(ncutUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_NCUT));

        User user = userRepository.findById(userUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        Comment comment = Comment.builder()
            .content(request.getContent())
            .ncut(ncut)
            .user(user)
            .build();

        commentRepository.save(comment);

        // 댓글 추가 후 갱신된 리스트 반환
        return getComments(ncutUuid, pageNum, display);
    }

    @Override
    public void updateComment(String commentUuid, CommentUpdateRequest request, String userUuid) {
        Comment comment = commentRepository.findById(commentUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));

        if (!comment.getUser().getUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        comment.setContent(request.getContent());
    }

    @Override
    public void deleteComment(String commentUuid, String userUuid) {
        Comment comment = commentRepository.findById(commentUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_COMMENT));

        if (!comment.getUser().getUuid().equals(userUuid)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        commentRepository.delete(comment);
    }
}
