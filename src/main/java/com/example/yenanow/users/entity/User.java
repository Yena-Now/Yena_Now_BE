package com.example.yenanow.users.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    @Column(name = "user_uuid", updatable = false, nullable = false)
    private String uuid;

    @Column(name = "email", length = 50, nullable = false)
    private String email;

    @Column(name = "name", length = 20)
    private String name;

    @Column(name = "nickname", length = 50, nullable = false, unique = true)
    private String nickname;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(name = "birthdate")
    private LocalDate birthdate;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "password", length = 100, nullable = false)
    private String password;

    @Column(name = "profile_url", length = 200)
    private String profileUrl;

    @Column(name = "total_cut")
    private int totalCut;

    @Column(name = "following_count")
    private int followingCount;

    @Column(name = "follower_count")
    private int followerCount;

    @CreatedDate
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public User updateProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;

        return this;
    }

    public void encodePassword(BCryptPasswordEncoder encoder) {
        this.password = encoder.encode(this.password);
    }
}

