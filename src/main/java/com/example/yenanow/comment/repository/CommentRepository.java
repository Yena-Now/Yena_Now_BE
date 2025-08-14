package com.example.yenanow.comment.repository;

import com.example.yenanow.comment.dto.query.CommentQueryDto;
import com.example.yenanow.comment.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommentRepository extends JpaRepository<Comment, String> {

    Page<Comment> findByNcutNcutUuid(String ncutUuid, Pageable pageable);

    @Query(value = "SELECT " +
        "    c.comment_uuid AS commentUuid, " +
        "    c.content AS content, " +
        "    c.created_at AS createdAt, " +
        "    u.user_uuid AS userUuid, " +
        "    u.name AS name, " +
        "    u.nickname AS nickname, " +
        "    u.profile_url AS profileUrl, " +
        "    u.deleted_at AS deletedAt " +
        "FROM ncut_comment c " +
        "JOIN users u ON c.user_uuid = u.user_uuid " +
        "WHERE c.ncut_uuid = :ncutUuid " +
        "ORDER BY c.created_at ASC",
        countQuery = "SELECT count(*) FROM ncut_comment WHERE c.ncut_uuid = :ncutUuid",
        nativeQuery = true)
    Page<CommentQueryDto> findCommentsWithUserInfo(@Param("ncutUuid") String ncutUuid,
        Pageable pageable);
}