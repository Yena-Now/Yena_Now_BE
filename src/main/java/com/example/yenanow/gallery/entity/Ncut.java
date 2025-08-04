package com.example.yenanow.gallery.entity;

import com.example.yenanow.comment.entity.Comment;
import com.example.yenanow.users.entity.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "ncut")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Ncut {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "ncut_uuid", length = 36, nullable = false)
    private String ncutUuid;

    @Column(name = "ncut_url", length = 200, nullable = false)
    private String ncutUrl;

    @Column(name = "thumbnail_url", length = 200, nullable = false)
    private String thumbnailUrl;

    @Column(name = "content", columnDefinition = "TEXT", nullable = true)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "is_relay", columnDefinition = "TINYINT(1) DEFAULT 0", nullable = false)
    private boolean isRelay = false;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "comment_cnt", nullable = false)
    private int commentCount = 0;

    @CreatedDate
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // 유저 연관관계 (FK: user_uuid)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_uuid")  // referencedColumnName 제거
    private User user;

    @OneToMany(mappedBy = "ncut", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}