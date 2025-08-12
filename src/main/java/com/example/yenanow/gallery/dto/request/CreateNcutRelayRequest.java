package com.example.yenanow.gallery.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateNcutRelayRequest {

    private Integer timeLimit;
    private Integer takeCount;
    private Integer cutCount;
    private String backgroundUrl;
    private String frameUuid;
    private List<ParticipantItem> participants;
    private List<CutItem> cuts;
}
