package com.example.yenanow.gallery.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Visibility {
    PRIVATE,
    FOLLOW,
    PUBLIC;

    public static Visibility from(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Visibility.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}