package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.Ncut;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyNcutResponse {

    private String ncutUuid;
    private String ncutURL;
    private String thumbnailUrl;
    private int likeCount;
    private boolean isRelay;

    public static MyNcutResponse fromEntity(Ncut ncut) {
        return new MyNcutResponse(
            ncut.getNcutUuid(),
            ncut.getNcutUrl(),
            ncut.getThumbnailUrl(),
            ncut.getLikeCount(),
            ncut.isRelay()
        );
    }
}
