package com.example.yenanow.openvidu.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {

    String token;
    String backgroundUrl;
    Integer takeCount;
    Integer cutCount;
    Integer timeLimit;
    List<String> cuts;
}
