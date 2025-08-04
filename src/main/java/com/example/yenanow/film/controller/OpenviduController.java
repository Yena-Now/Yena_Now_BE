package com.example.yenanow.film.controller;

import com.example.yenanow.film.dto.request.CodeRequest;
import com.example.yenanow.film.dto.request.TokenRequest;
import com.example.yenanow.film.dto.response.CodeResponse;
import com.example.yenanow.film.dto.response.TokenResponse;
import com.example.yenanow.film.service.OpenviduService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/film")
public class OpenviduController {

    private final OpenviduService openviduService;

    @PostMapping(value = "/code")
    public ResponseEntity<CodeResponse> createCode(
        @AuthenticationPrincipal Object principal,
        @RequestBody CodeRequest codeRequest) {
        String userUuid = principal.toString();
        CodeResponse codeResponse = openviduService.createCode(userUuid, codeRequest);
        return ResponseEntity.ok(codeResponse);
    }

    @PostMapping(value = "/token")
    public ResponseEntity<TokenResponse> createToken(
        @AuthenticationPrincipal Object principal,
        @RequestBody TokenRequest tokenRequest) {
        String userUuid = principal.toString();
        TokenResponse tokenResponse = openviduService.createToken(userUuid, tokenRequest);
        return ResponseEntity.ok(tokenResponse);
    }

    @PostMapping(value = "/livekit/webhook", consumes = "application/webhook+json")
    public ResponseEntity<String> receiveWebhook(
        @RequestHeader("Authorization") String authHeader,
        @RequestBody String body) {
        openviduService.reciveWebhook(authHeader, body);
        return ResponseEntity.ok("ok");
    }
}
