package com.example.yenanow.gallery.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

@Entity
@Table(name = "ncut")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
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

    @Column(name = "is_relay", nullable = false)
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

    @Column(name = "user_uuid", length = 36, nullable = false)
    private String userUuid;

}
