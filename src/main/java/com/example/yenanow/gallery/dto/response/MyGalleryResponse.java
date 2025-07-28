package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.Ncut;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

@Getter
@AllArgsConstructor
public class MyGalleryResponse {

    private int totalPages;
    private List<MyNcutResponse> ncuts;

    public static MyGalleryResponse fromPage(Page<Ncut> page) {
        List<MyNcutResponse> ncutDtos = page.getContent().stream()
            .map(MyNcutResponse::fromEntity)
            .collect(Collectors.toList());
        return new MyGalleryResponse(page.getTotalPages(), ncutDtos);
    }
}
