package com.example.yenanow.gallery.dto.response;

import com.example.yenanow.gallery.entity.Relay;
import com.example.yenanow.s3.service.S3Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RelayListItem {

    private String relayUuid;
    private Integer timeLimit;
    private LocalDateTime expiredAt;
    private Integer takeCount;
    private Integer cutCount;
    private LocalDateTime createdAt;
    private List<RelayParticipantItem> participants;

    public static RelayListItem fromEntity(Relay relay, S3Service s3Service) {
        return RelayListItem.builder()
            .relayUuid(relay.getRelayUuid())
            .timeLimit(relay.getTimeLimit())
            .expiredAt(relay.getExpiredAt())
            .takeCount(relay.getTakeCount())
            .cutCount(relay.getCutCount())
            .createdAt(relay.getCreatedAt())
            .participants(relay.getParticipants().stream()
                .map(participant -> RelayParticipantItem.fromEntity(participant, s3Service))
                .collect(Collectors.toList()))
            .build();
    }
}
