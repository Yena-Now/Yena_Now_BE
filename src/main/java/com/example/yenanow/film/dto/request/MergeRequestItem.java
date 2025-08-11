package com.example.yenanow.film.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MergeRequestItem {

    private String contentUrl;
    private Integer order;
}
