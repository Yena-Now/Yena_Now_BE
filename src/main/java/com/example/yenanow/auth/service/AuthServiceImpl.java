package com.example.yenanow.auth.service;

import com.example.yenanow.auth.dto.request.ForgotPasswordRequest;
import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.smtp.MailService;
import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import com.example.yenanow.common.util.CookieUtil;
import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final BCryptPasswordEncoder encoder;
    private final MailService mailService;
    private final JwtUtil jwtUtil;

    private static final long VERIFICATION_CODE_TTL_MINUTES = 5;

    @Value("${jwt.refresh-token-expiration}")
    private int refreshTokenExpiration;

    @Override
    public LoginResponse login(LoginRequest loginRequest, HttpServletResponse response) {
        User user = userRepository.findByEmail(loginRequest.getEmail())
            .orElseThrow(() -> new BusinessException(ErrorCode.INVALID_SIGNIN));

        if (!encoder.matches(loginRequest.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.INVALID_SIGNIN);
        }

        String token = jwtUtil.generateToken(user.getUserUuid());
        String refreshToken = jwtUtil.generateRefreshToken(user.getUserUuid());

        // 쿠키에 refreshToken 저장
        CookieUtil.addHttpOnlyCookie(response, "refresh_token", refreshToken,
            refreshTokenExpiration);

        return LoginResponse.builder()
            .accessToken(token)
            .userUuid(user.getUserUuid())
            .nickname(user.getNickname())
            .profileUrl(user.getProfileUrl())
            .build();
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String userUuid = (String) request.getAttribute("userUuid");
        String key = "refresh_token:" + userUuid;
        redisTemplate.delete(key); // redis에서 리프레시 토큰 삭제
        CookieUtil.deleteCookie(request, response, "refresh_token"); // 쿠키에 저장된 리프레시 토큰 삭제
    }

    @Override
    public void sendMessage(VerificationEmailRequest request) {
        String email = request.getEmail();
        userRepository.findByEmail(email) // 등록된 유저 이메일인지 여부
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        String code = String.format("%06d", new Random().nextInt(999999));

        String key = "email:" + email;
        redisTemplate.opsForValue()
            .set(key, code, Duration.ofMinutes(VERIFICATION_CODE_TTL_MINUTES));

        String subject = "[YenaNow] 이메일 인증";
        String content = "인증 코드: " + code + "\n5분 내로 인증을 완료해주세요.";

        mailService.sendEmail(email, subject, content);
    }

    @Override
    public VerifyEmailResponse verifyMessage(VerifyEmailRequest request) {
        String email = request.getEmail();
        String key = "email:" + email;
        String code = redisTemplate.opsForValue().get(key);

        boolean isVerified = code != null && code.equals(request.getCode());

        if (isVerified) {
            // 인증했는지 여부 redis에 저장해 임시비밀번호 요청에 사용
            redisTemplate.opsForValue().set("verified:" + email, "true", Duration.ofMinutes(5));
            redisTemplate.delete(key); // 인증 성공 시 Redis에서 삭제
        }

        return new VerifyEmailResponse(isVerified);
    }

    @Override
    public void sendPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        String key = "verified:" + email;
        String verified = redisTemplate.opsForValue().get(key);

        if (!verified.equals("true")) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        String tempPassword = generatePassword(12);

        user.setPassword(encoder.encode(tempPassword));
        userRepository.save(user);

        String subject = "[YenaNow] 임시 비밀번호 발급 안내";
        String content = "임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경해주세요.";
        mailService.sendEmail(email, subject, content);

        redisTemplate.delete(key);
    }

    @Override
    public String reissueToken(HttpServletRequest request) {
        String refreshToken = CookieUtil.getCookieValue(request, "refresh_token");

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        String userUuid = jwtUtil.getSubject(refreshToken); // 토큰에서 사용자 UUID 추출

        String storedRefreshToken = redisTemplate.opsForValue().get("refresh_token:" + userUuid);
        if (!refreshToken.equals(storedRefreshToken)) { // redis에 저장된 리프레시 토큰인지 검증
            throw new BusinessException(ErrorCode.DUPLICATE_SIGNIN_DETECTED);
        }

        return jwtUtil.generateToken(userUuid);
    }

    private String generatePassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}