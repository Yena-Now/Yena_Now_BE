package com.example.yenanow.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupResponse {

    private String accessToken;
    private String userUuid;
    private String nickname;
    private String profileUrl;
}