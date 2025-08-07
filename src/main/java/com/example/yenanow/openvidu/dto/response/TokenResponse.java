package com.example.yenanow.openvidu.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {

    String token;
    String backgroundUrl;
    Integer takeCnt;
    Integer cutCnt;
    Integer timeLimit;
    List<String> cuts;
}
