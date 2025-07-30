package com.example.yenanow.comment.repository;

import com.example.yenanow.comment.entity.Comment;
import com.example.yenanow.gallery.entity.Ncut;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, String> {

    Page<Comment> findByNcut(Ncut ncut, Pageable pageable);
}