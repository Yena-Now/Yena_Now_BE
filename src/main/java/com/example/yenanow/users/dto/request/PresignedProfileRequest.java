package com.example.yenanow.users.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PresignedProfileRequest {

    private String fileName;

    private String contentType;
}
