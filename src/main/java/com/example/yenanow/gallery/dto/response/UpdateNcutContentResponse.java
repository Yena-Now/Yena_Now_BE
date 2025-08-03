package com.example.yenanow.gallery.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateNcutContentResponse {

    String ncutUuid;
    String content;
    LocalDateTime updatedAt;
}
