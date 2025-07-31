package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.Ncut;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyGalleryResponse {

    private int totalPages;
    private List<MyNcutResponse> ncuts;

    // 유저 정보 없는 응답 - ex) 내 갤러리, 타인 갤러리
    public static MyGalleryResponse fromEntity(Page<Ncut> page) {
        return MyGalleryResponse.builder()
            .totalPages(page.getTotalPages())
            .ncuts(page.getContent().stream()
                .map(MyNcutResponse::fromEntity)
                .collect(Collectors.toList()))
            .build();
    }

    // 유저 정보 포함 응답 - ex) 공개 갤러리, 친구 갤러리
    public static MyGalleryResponse fromEntityWithUser(Page<Ncut> page) {
        return MyGalleryResponse.builder()
            .totalPages(page.getTotalPages())
            .ncuts(page.getContent().stream()
                .map(MyNcutResponse::fromEntityWithUser)
                .collect(Collectors.toList()))
            .build();
    }
}