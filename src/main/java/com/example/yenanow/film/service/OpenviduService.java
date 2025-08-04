package com.example.yenanow.film.service;

import com.example.yenanow.film.dto.request.CodeRequest;
import com.example.yenanow.film.dto.request.TokenRequest;
import com.example.yenanow.film.dto.response.CodeResponse;
import com.example.yenanow.film.dto.response.TokenResponse;

public interface OpenviduService {

    CodeResponse createCode(String userUuid, CodeRequest codeRequest);

    TokenResponse createToken(String userUuid, TokenRequest tokenRequest);

    void reciveWebhook(String authHeader, String body);
}
