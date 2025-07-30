package com.example.yenanow.openvidu.service;

import com.example.yenanow.openvidu.dto.request.CodeRequest;
import com.example.yenanow.openvidu.dto.response.CodeResponse;

public interface OpenviduService {

    CodeResponse createCode(String userUuid, CodeRequest codeRequest);
}
