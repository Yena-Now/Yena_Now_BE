package com.example.yenanow.gallery.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateRelayRequest {

    private String relayUuid;
    private String frameUuid;
    private List<UpdateRelayItem> cuts;
}
