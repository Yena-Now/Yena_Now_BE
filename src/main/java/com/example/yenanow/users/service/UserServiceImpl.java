package com.example.yenanow.users.service;

import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.response.SignupResponse;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;

    @Override
    public SignupResponse addUser(SignupRequest signupRequest) {
        User user = signupRequest.toEntity();
        user.encodePassword(encoder);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        user = userRepository.save(user); // 저장 후 UUID 획득

        String token = jwtUtil.generateToken(user.getUuid());

        return SignupResponse.builder()
            .accessToken(token)
            .userUuid(user.getUuid())
            .nickname(user.getNickname())
            .profileUrl(user.getProfileUrl())
            .build();
    }
}
