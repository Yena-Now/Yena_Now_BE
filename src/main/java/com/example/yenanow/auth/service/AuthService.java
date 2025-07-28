package com.example.yenanow.auth.service;


import com.example.yenanow.auth.dto.request.ForgotPasswordRequest;
import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.request.VerificationEmailRequest;
import com.example.yenanow.auth.dto.request.VerifyEmailRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import com.example.yenanow.auth.dto.response.VerifyEmailResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest, HttpServletResponse response);

    void sendVerification(VerificationEmailRequest request);

    VerifyEmailResponse verifyEmailCode(VerifyEmailRequest request);

    void sendTemporaryPassword(ForgotPasswordRequest request);

    String reissueAccessToken(HttpServletRequest request);
}
