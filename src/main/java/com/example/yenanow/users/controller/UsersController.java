package com.example.yenanow.users.controller;

import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.dto.request.SignupRequest;
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

        return ResponseEntity.ok(userService.addUser(signupRequest));
    }

}
