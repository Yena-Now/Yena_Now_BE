package com.example.yenanow.users.controller;

import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.response.SignupResponse;
import com.example.yenanow.users.service.UserService;
import java.util.UUID;
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
        userService.addUser(signupRequest);

        String userUuid = UUID.randomUUID().toString();

        String accessToken = JwtUtil.generateToken(userUuid);

        SignupResponse response = SignupResponse.builder()
            .accessToken(accessToken)
            .userUuid(userUuid)
            .nickname(signupRequest.getNickname())
            .profileUrl(null)
            .build();

        return ResponseEntity.ok(response);
    }

}
