package com.example.yenanow.openvidu.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {

    String token;
    String backgoundUrl;
    Integer takeCnt;
    Integer cutCnt;
    Integer timeLimit;
}
