package com.example.yenanow.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserSearchResponseItem {

    private String userUuid;
    private String profileUrl;
    private String name;
    private String nickname;

    @JsonProperty("isFollowing")
    private Boolean following;
}