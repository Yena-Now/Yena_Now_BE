package com.example.yenanow.auth.oauth;

import com.example.yenanow.common.util.CookieUtil;
import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${client.origin}")
    private String clientOrigin;

    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    public OAuth2LoginSuccessHandler(JwtUtil jwtUtil, UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.userRepository = userRepository;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
        HttpServletResponse response,
        Authentication authentication)
        throws IOException, ServletException {
        org.springframework.security.oauth2.core.user.OAuth2User oAuth2User =
            (org.springframework.security.oauth2.core.user.OAuth2User) authentication.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String provider = oAuth2User.getAttribute("provider");

        Optional<User> userOpt = userRepository.findByEmailAndProvider(email, provider);
        if (userOpt.isEmpty()) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "User not found");
            return;
        }

        User user = userOpt.get();

        // 닉네임을 URL 인코딩
        String encodedNickname = URLEncoder.encode(user.getNickname(), StandardCharsets.UTF_8);

        // JWT 생성
        String accessToken = jwtUtil.generateToken(user.getUserUuid());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserUuid());

        // 쿠키에 refreshToken 저장
        CookieUtil.addHttpOnlyCookie(response, "refresh_token", refreshToken,
            refreshTokenExpiration);

        String redirectUrl = clientOrigin + "?accessToken=" + accessToken +
            "&userUuid=" + user.getUserUuid() +
            "&nickname=" + encodedNickname +
            "&profileUrl=" + user.getProfileUrl();
        response.sendRedirect(redirectUrl);
    }
}