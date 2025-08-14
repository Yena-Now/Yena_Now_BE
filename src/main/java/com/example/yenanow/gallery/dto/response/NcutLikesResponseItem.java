package com.example.yenanow.gallery.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NcutLikesResponseItem {

    private String userUuid;
    private String name;
    private String nickname;
    private String profileUrl;
}
