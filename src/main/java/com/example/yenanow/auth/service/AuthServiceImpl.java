package com.example.yenanow.auth.service;

import com.example.yenanow.auth.dto.request.ForgotPasswordRequest;
import com.example.yenanow.auth.dto.request.LoginRequest;
import com.example.yenanow.auth.dto.request.VerificationEmailRequest;
import com.example.yenanow.auth.dto.request.VerifyEmailRequest;
import com.example.yenanow.auth.dto.response.LoginResponse;
import com.example.yenanow.auth.dto.response.VerifyEmailResponse;
import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;
    private final MailService mailService;

    private static final long VERIFICATION_CODE_TTL_MINUTES = 5;

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

    @Override
    public void sendVerification(VerificationEmailRequest request) {
        String email = request.getEmail();
        String code = String.format("%06d", new Random().nextInt(999999));

        String key = "email:" + email;
        redisTemplate.opsForValue()
            .set(key, code, Duration.ofMinutes(VERIFICATION_CODE_TTL_MINUTES));

        String subject = "[YenaNow] 이메일 인증";
        String content = "인증 코드: " + code + "\n5분 내로 인증을 완료해주세요.";

        mailService.sendEmail(email, subject, content);
    }

    @Override
    public VerifyEmailResponse verifyEmailCode(VerifyEmailRequest request) {
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
    public void sendTemporaryPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();

        String key = "verified:" + email;
        String verified = redisTemplate.opsForValue().get(key);

        if (!verified.equals("true")) {
            throw new RuntimeException("이메일 인증이 완료되지 않았습니다");
        }

        User user = userRepository.findByEmail(email) // 등록된 유저 이메일인지 여부
            .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));

        String tempPassword = generateRandomPassword(12);

        user.setPassword(encoder.encode(tempPassword));
        userRepository.save(user);

        String subject = "[YenaNow] 임시 비밀번호 발급 안내";
        String content = "임시 비밀번호: " + tempPassword + "\n로그인 후 반드시 비밀번호를 변경해주세요.";
        mailService.sendEmail(email, subject, content);

        redisTemplate.delete(key);
    }

    private String generateRandomPassword(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}