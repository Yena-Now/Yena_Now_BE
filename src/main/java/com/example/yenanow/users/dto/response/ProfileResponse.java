package com.example.yenanow.users.dto.response;

import com.example.yenanow.users.entity.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class ProfileResponse {

    private String name;
    private String nickname;
    private Gender gender;
    private String profileUrl;
    private int followingCount;
    private int followerCount;
    private int totalCut;
    private boolean isFollowing;
    private boolean isMine;
}