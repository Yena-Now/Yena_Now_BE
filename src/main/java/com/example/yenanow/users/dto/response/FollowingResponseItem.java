package com.example.yenanow.users.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FollowingResponseItem {

    private String userUuid;
    private String name;
    private String nickname;
    private String profileUrl;

    @JsonProperty("isFollowing")
    private boolean following;
}
