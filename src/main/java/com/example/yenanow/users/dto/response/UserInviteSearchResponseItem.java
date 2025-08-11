package com.example.yenanow.users.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserInviteSearchResponseItem {

    private String userUuid;
    private String profileUrl;
    private String name;
    private String nickname;
}