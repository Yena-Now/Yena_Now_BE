package com.example.yenanow.gallery.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateRelayItem {

    private String cutUrl;
    private String cutIndex;
    private Boolean isTaken;
}
