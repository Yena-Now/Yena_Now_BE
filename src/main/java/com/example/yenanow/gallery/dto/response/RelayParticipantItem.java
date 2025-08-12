package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.RelayParticipant;
import com.example.yenanow.s3.service.S3Service;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelayParticipantItem {

    private String userUuid;
    private String nickname;
    private String profileUrl;

    public static RelayParticipantItem fromEntity(RelayParticipant participant,
        S3Service s3Service) {
        return RelayParticipantItem.builder()
            .userUuid(participant.getUser().getUserUuid())
            .nickname(participant.getUser().getNickname())
            .profileUrl(s3Service.getFileUrl(participant.getUser().getProfileUrl()))
            .build();
    }
}
