package com.example.yenanow.auth.service;


import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.request.VerificationEmailRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {

    LoginResponse login(LoginRequest loginRequest, HttpServletResponse response);

    void sendVerification(VerificationEmailRequest request);
}
