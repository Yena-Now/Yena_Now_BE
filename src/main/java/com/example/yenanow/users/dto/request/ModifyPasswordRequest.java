package com.example.yenanow.users.dto.request;

import lombok.Getter;

@Getter
public class ModifyPasswordRequest {

    private String oldPassword;
    private String newPassword;
}