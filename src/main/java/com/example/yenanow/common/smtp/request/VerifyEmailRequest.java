package com.example.yenanow.common.smtp.request;

import lombok.Getter;

@Getter
public class VerifyEmailRequest {

    private String email;
    private String code;
}
