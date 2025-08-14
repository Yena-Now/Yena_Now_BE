package com.example.yenanow.gallery.dto.query;

import java.time.LocalDateTime;

public interface LikeUserQueryDto {

    String getUserUuid();

    String getName();

    String getNickname();

    String getProfileUrl();

    LocalDateTime getDeletedAt();
}