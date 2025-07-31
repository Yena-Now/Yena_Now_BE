package com.example.yenanow.auth.controller;

import com.example.yenanow.auth.dto.request.ForgotPasswordRequest;
import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import com.example.yenanow.auth.dto.response.ReissueTokenResponse;
import com.example.yenanow.auth.service.AuthService;
import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "인증 관련 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "자체 로그인", description = "이메일과 비밀번호를 이용해 로그인을 수행합니다.")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest,
        HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    @Operation(summary = "로그아웃", description = "로그아웃을 수행합니다.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request, response);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "이메일 인증코드 발송", description = "사용자 이메일로 인증 코드를 전송합니다.")
    @PostMapping("/verification-email")
    public ResponseEntity<Void> sendMessage(@RequestBody VerificationEmailRequest request) {
        authService.sendMessage(request);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "이메일로 받은 인증 코드를 검증합니다.")
    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyMessage(
        @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyMessage(request));
    }

    @Operation(summary = "임시 비밀번호 발급", description = "이메일로 임시 비밀번호를 전송합니다.")
    @PostMapping("/password")
    public ResponseEntity<Void> sendPassword(@RequestBody ForgotPasswordRequest request) {
        authService.sendPassword(request);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "토큰 재발급", description = "리프레시 토큰을 이용해 액세스 토큰을 재발급합니다.")
    @PostMapping("/tokens")
    public ResponseEntity<ReissueTokenResponse> reissueToken(HttpServletRequest request) {
        String accessToken = authService.reissueToken(request);
        return ResponseEntity.ok(new ReissueTokenResponse(accessToken));
    }
}