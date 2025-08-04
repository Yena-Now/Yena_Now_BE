package com.example.yenanow.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class UpdateProfileUrlResponse {

    private String imageUrl;
}
