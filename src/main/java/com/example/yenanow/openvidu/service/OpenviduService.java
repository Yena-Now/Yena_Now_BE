package com.example.yenanow.openvidu.service;

import com.example.yenanow.openvidu.dto.request.CodeRequest;
import com.example.yenanow.openvidu.dto.request.TokenRelayRequest;
import com.example.yenanow.openvidu.dto.request.TokenRequest;
import com.example.yenanow.openvidu.dto.response.CodeResponse;
import com.example.yenanow.openvidu.dto.response.TokenResponse;

public interface OpenviduService {

    CodeResponse createCode(String userUuid, CodeRequest codeRequest);

    TokenResponse createToken(String userUuid, TokenRequest tokenRequest);

    void reciveWebhook(String authHeader, String body);

    TokenResponse createRelayToken(String userUuid, TokenRelayRequest tokenRelayRequest);

    void addCutKeyToRoom(String roomCode, String cutKey);
}
