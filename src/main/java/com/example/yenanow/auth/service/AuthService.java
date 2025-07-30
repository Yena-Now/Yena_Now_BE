package com.example.yenanow.auth.service;


import com.example.yenanow.auth.dto.request.ForgotPasswordRequest;
import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest, HttpServletResponse response);

    void logout(HttpServletRequest request, HttpServletResponse response);

    void sendMessage(VerificationEmailRequest request);

    VerifyEmailResponse verifyMessage(VerifyEmailRequest request);

    void sendPassword(ForgotPasswordRequest request);

    String reissueToken(HttpServletRequest request);
}
