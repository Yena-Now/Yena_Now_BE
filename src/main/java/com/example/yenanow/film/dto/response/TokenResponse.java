package com.example.yenanow.film.dto.response;

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
