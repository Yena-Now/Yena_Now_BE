package com.example.yenanow.gallery.dto.request;

import com.example.yenanow.gallery.entity.Visibility;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CreateRelayNcutRequest {

    private String relayUuid;
    private String ncutUrl;
    private String thumbnailUrl;
    private String content;
    private Visibility visibility;
    private Boolean isRelay;
}
