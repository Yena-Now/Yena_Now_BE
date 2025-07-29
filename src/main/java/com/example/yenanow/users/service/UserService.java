package com.example.yenanow.users.service;

import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import com.example.yenanow.users.dto.request.NicknameRequest;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.response.NicknameResponse;
import com.example.yenanow.users.dto.response.SignupResponse;

public interface UserService {

    SignupResponse createUser(SignupRequest signupRequest);

    NicknameResponse validateNickname(NicknameRequest nicknameRequest);

    void sendVerification(VerificationEmailRequest request);

    VerifyEmailResponse verifyEmailCode(VerifyEmailRequest request);
}
