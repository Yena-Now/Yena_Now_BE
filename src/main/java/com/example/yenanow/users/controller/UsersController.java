package com.example.yenanow.users.controller;

import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import com.example.yenanow.users.dto.request.NicknameRequest;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.response.NicknameResponse;
import com.example.yenanow.users.dto.response.SignupResponse;
import com.example.yenanow.users.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(userService.createUser(signupRequest));
    }

    @PostMapping("/nickname")
    public ResponseEntity<NicknameResponse> validateNickname(
        @RequestBody NicknameRequest nicknameRequest) {
        return ResponseEntity.ok(userService.validateNickname(nicknameRequest));
    }

    @PostMapping("/verification-email")
    public ResponseEntity<Void> sendMessage(@RequestBody VerificationEmailRequest request) {
        userService.sendVerification(request);
        return ResponseEntity.noContent().build(); // 204
    }

    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyMessage(
        @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(userService.verifyEmailCode(request));
    }
}
