package com.example.yenanow.film.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CodeRequest {

    String backgroundUrl;
    Integer takeCnt;
    Integer cutCnt;
    Integer timeLimit;
}
