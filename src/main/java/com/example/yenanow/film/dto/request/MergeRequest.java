package com.example.yenanow.film.dto.request;

import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MergeRequest {

    private String frameUuid;
    private List<MergeRequestItem> contentUrls;
}
