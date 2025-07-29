package com.example.yenanow.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NicknameResponse {

    @JsonProperty("isDuplicated")
    private boolean duplicated;
}