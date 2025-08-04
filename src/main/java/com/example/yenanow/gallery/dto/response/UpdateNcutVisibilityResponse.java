package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.Visibility;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateNcutVisibilityResponse {

    private String ncutUuid;
    private Visibility visibility;
    private LocalDateTime updatedAt;
}
