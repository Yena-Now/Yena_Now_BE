package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.Ncut;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NcutRankingResponse {

    private String ncutUuid;
    private String ncutUrl;
    private int likeCount;

    public static NcutRankingResponse fromEntity(Ncut ncut) {
        return NcutRankingResponse.builder()
            .ncutUuid(ncut.getNcutUuid())
            .ncutUrl(ncut.getNcutUrl())
            .likeCount(ncut.getLikeCount())
            .build();
    }
}
