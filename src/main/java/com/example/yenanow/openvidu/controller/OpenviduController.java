package com.example.yenanow.openvidu.controller;

import com.example.yenanow.openvidu.dto.request.CodeRequest;
import com.example.yenanow.openvidu.dto.request.TokenRequest;
import com.example.yenanow.openvidu.dto.response.CodeResponse;
import com.example.yenanow.openvidu.dto.response.TokenResponse;
import com.example.yenanow.openvidu.service.OpenviduService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "OpenVidu", description = "OpenVidu 연동 및 Livekit Webhook 처리 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/openvidu")
public class OpenviduController {

    private final OpenviduService openviduService;

    @Operation(summary = "참가 코드 생성", description = "사용자의 OpenVidu 세션 참가 코드를 생성합니다.")
    @PostMapping(value = "/code")
    public ResponseEntity<CodeResponse> createCode(
        @Parameter(hidden = true) @AuthenticationPrincipal Object principal,
        @RequestBody CodeRequest codeRequest) {
        String userUuid = principal.toString();
        CodeResponse codeResponse = openviduService.createCode(userUuid, codeRequest);
        return ResponseEntity.ok(codeResponse);
    }

    @Operation(summary = "토큰 발급", description = "사용자의 OpenVidu 접속 토큰을 발급합니다.")
    @PostMapping(value = "/token")
    public ResponseEntity<TokenResponse> createToken(
        @Parameter(hidden = true) @AuthenticationPrincipal Object principal,
        @RequestBody TokenRequest tokenRequest) {
        String userUuid = principal.toString();
        TokenResponse tokenResponse = openviduService.createToken(userUuid, tokenRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    @Operation(
        summary = "Livekit Webhook 처리",
        description = "Livekit에서 발송한 Webhook 이벤트를 수신하고 처리합니다."
    )
    @PostMapping(value = "/livekit/webhook", consumes = "application/webhook+json")
    public ResponseEntity<String> receiveWebhook(
        @Parameter(description = "인증 헤더", example = "Bearer abc123")
        @RequestHeader("Authorization") String authHeader,
        @RequestBody String body) {
        openviduService.reciveWebhook(authHeader, body);
        return ResponseEntity.ok("ok");
    }
}