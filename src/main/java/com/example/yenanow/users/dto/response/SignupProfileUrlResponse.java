package com.example.yenanow.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupProfileUrlResponse {

    private String uploadUrl;

    private String fileUrl;
}
