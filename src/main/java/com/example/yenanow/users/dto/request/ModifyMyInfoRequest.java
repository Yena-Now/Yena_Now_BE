package com.example.yenanow.users.dto.request;

import lombok.Getter;

@Getter
public class ModifyMyInfoRequest {

    private String name;
    private String nickname;
    private String gender;
    private String birthdate;
    private String phoneNumber;
}
