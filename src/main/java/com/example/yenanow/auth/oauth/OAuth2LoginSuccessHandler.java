package com.example.yenanow.auth.oauth;

import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication)
        throws IOException, ServletException {

        String email = ((org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal())
            .getAttribute("email");


        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        User user = userOpt.get();

        // JWT 생성
        String accessToken = jwtUtil.generateToken(user.getUuid());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUuid());

        // 클라이언트에 JSON으로 응답
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        String json = String.format("""
            {
              "accessToken": "%s",
              "refreshToken": "%s",
              "userUuid": "%s",
              "nickname": "%s",
              "profileUrl": "%s"
            }
            """, accessToken, refreshToken, user.getUuid(), user.getNickname(), user.getProfileUrl());
        response.getWriter().write(json);
    }
}