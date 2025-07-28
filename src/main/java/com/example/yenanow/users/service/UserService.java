package com.example.yenanow.users.service;

import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.response.SignupResponse;

public interface UserService {

    SignupResponse createUser(SignupRequest signupRequest);
}
