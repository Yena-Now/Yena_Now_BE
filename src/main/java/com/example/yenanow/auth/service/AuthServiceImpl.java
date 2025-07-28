package com.example.yenanow.auth.service;

import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다."));

        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new RuntimeException("아이디 또는 비밀번호가 일치하지 않습니다.");
        }

        String token = jwtUtil.generateToken(user.getUuid());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUuid());

        // 쿠키에 refreshToken 저장
        Cookie refreshTokenCookie = new Cookie("refreshToken", refreshToken);
        refreshTokenCookie.setHttpOnly(true); // JS에서 접근 못하도록
        refreshTokenCookie.setSecure(true); // HTTPS에서만 전송되도록
        refreshTokenCookie.setPath("/"); // 모든 경로에서 접근 가능하도록
        refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(refreshTokenCookie);

        return LoginResponse.builder()
            .accessToken(token)
            .userUuid(user.getUuid())
            .nickname(user.getNickname())
            .profileUrl(user.getProfileUrl())
            .build();
    }
}