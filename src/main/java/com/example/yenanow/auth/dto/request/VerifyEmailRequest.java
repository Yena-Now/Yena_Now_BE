package com.example.yenanow.auth.dto.request;

import lombok.Getter;

@Getter
public class VerifyEmailRequest {

    private String email;
    private String code;
}
