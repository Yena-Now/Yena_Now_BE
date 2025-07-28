package com.example.yenanow.auth.service;


import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
}
