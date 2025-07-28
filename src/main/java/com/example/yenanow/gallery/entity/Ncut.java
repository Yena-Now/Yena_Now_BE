package com.example.yenanow.gallery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ncut")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ncut {

    @Id
    @Column(name = "ncut_uuid", length = 36, nullable = false)
    private String ncutUuid;

    @Column(name = "ncut_url", length = 200, nullable = false)
    private String ncutUrl;

    @Column(name = "thumbnail_url", length = 200, nullable = false)
    private String thumbnailUrl;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "visibility", nullable = false)
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "is_relay", nullable = false)
    private boolean isRelay = false;

    @Column(name = "like_count", nullable = false)
    private int likeCount = 0;

    @Column(name = "comment_cnt", nullable = false)
    private int commentCount = 0;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "user_uuid", length = 36, nullable = false)
    private String userUuid;

    @PrePersist
    public void prePersist() {
        if (this.ncutUuid == null) {
            this.ncutUuid = UUID.randomUUID().toString();
        }
        if (this.createdAt == null) {
            this.createdAt = LocalDateTime.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
