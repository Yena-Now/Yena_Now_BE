package com.example.yenanow.users.service;

import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.entity.User;

public interface UserService {

    public User addUser(SignupRequest signupRequest);

}
