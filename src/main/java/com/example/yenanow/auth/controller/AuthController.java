package com.example.yenanow.auth.controller;

import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import com.example.yenanow.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    public ResponseEntity<String> testAuth(@AuthenticationPrincipal Object principal) {
        String userUuid = principal.toString();  // JwtAuthenticationFilter에서 세팅한 UUID
        return ResponseEntity.ok("인증 성공! 사용자 UUID: " + userUuid);
    }
}
