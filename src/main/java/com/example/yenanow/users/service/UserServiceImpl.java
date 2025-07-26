package com.example.yenanow.users.service;

import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public User addUser(SignupRequest signupRequest) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        User user = signupRequest.toEntity();
        user.encodePassword(encoder);

        return userRepository.save(user);
    }

}
