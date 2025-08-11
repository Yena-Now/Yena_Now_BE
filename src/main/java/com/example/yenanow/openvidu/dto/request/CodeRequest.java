package com.example.yenanow.openvidu.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CodeRequest {

    String backgroundUrl;
    Integer takeCount;
    Integer cutCount;
    Integer timeLimit;
}
