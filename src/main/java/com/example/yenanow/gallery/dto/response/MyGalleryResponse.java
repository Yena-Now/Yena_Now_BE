package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.Ncut;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@AllArgsConstructor
public class MyGalleryResponse {
    private int totalPages;
    private List<MyNcutDto> ncuts;

    public static MyGalleryResponse fromPage(Page<Ncut> page) {
        List<MyNcutDto> ncutDtos = page.getContent().stream()
                .map(MyNcutDto::fromEntity)
                .collect(Collectors.toList());
        return new MyGalleryResponse(page.getTotalPages(), ncutDtos);
    }
}
