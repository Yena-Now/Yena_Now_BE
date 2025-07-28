package com.example.yenanow.auth.controller;

import com.example.yenanow.auth.dto.request.ForgotPasswordRequest;
import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.request.VerificationEmailRequest;
import com.example.yenanow.auth.dto.request.VerifyEmailRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import com.example.yenanow.auth.dto.response.VerifyEmailResponse;
import com.example.yenanow.auth.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest,
        HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(loginRequest, response));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    @PostMapping("/verification-email")
    public ResponseEntity<Void> sendMessage(@RequestBody VerificationEmailRequest request) {
        authService.sendVerification(request);
        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyMessage(
        @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(authService.verifyEmailCode(request));
    }

    @PostMapping("/password")
    public ResponseEntity<Void> sendPassword(@RequestBody ForgotPasswordRequest request) {
        authService.sendTemporaryPassword(request);
        return ResponseEntity.noContent().build(); // 204
    }
}
