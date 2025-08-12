package com.example.yenanow.gallery.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NcutRelayListResponse {

    private int totalPages;
    List<RelayListItem> relay;
}
