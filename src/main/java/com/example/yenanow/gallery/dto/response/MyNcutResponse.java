package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.Ncut;
import com.example.yenanow.s3.service.S3Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MyNcutResponse {

    private String userUuid;
    private String profileUrl;
    private String nickname;
    private String ncutUuid;
    private String thumbnailUrl;
    private String ncutUrl;
    private int likeCount;
    private boolean isRelay;

    /**
     * 유저 정보 없이 (내 갤러리/타인 공개)
     */
    public static MyNcutResponse fromEntity(Ncut ncut, S3Service s3Service) {
        return MyNcutResponse.builder()
            .ncutUuid(ncut.getNcutUuid())
            .thumbnailUrl(s3Service.getFileUrl(ncut.getThumbnailUrl())) // 키 → URL 변환
            .ncutUrl(s3Service.getFileUrl(ncut.getNcutUrl()))
            .likeCount(ncut.getLikeCount())
            .isRelay(ncut.isRelay())
            .build();
    }

    /**
     * 유저 정보 포함 (팔로잉/공개 전체)
     */
    public static MyNcutResponse fromEntityWithUser(Ncut ncut, S3Service s3Service) {
        return MyNcutResponse.builder()
            .userUuid(ncut.getUser().getUserUuid())
            .profileUrl(s3Service.getFileUrl(ncut.getUser().getProfileUrl()))
            .nickname(ncut.getUser().getNickname())
            .ncutUuid(ncut.getNcutUuid())
            .thumbnailUrl(s3Service.getFileUrl(ncut.getThumbnailUrl()))
            .ncutUrl(s3Service.getFileUrl(ncut.getNcutUrl()))
            .likeCount(ncut.getLikeCount())
            .isRelay(ncut.isRelay())
            .build();
    }
}