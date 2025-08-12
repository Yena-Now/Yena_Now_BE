package com.example.yenanow.gallery.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CutItem {

    private String cutUrl;
    private String cutIndex;
    private Boolean isTaken;
}
