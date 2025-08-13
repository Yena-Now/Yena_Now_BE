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
import io.jsonwebtoken.JwtException;
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
        User user = userRepository.findByEmailAndProviderIsNull(loginRequest.getEmail())
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
        userRepository.findByEmailAndProviderIsNull(email) // 등록된 유저 이메일인지 여부
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        String code = String.format("%06d", new Random().nextInt(999999));

        String key = "email:" + email;
        redisTemplate.opsForValue()
            .set(key, code, Duration.ofMinutes(VERIFICATION_CODE_TTL_MINUTES));

        String subject = "[YenaNow] 이메일 인증";
        // CSS를 인라인으로 작성하여 이메일 클라이언트 호환성을 높임
        String htmlContent = String.format(
            "<!DOCTYPE html>" +
                "<html lang='ko'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>이메일 인증</title>" +
                "</head>" +
                "<body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Oxygen, Ubuntu, Cantarell, \"Open Sans\", \"Helvetica Neue\", sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;'>"
                +
                "<div style='max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.05); border: 1px solid #e0e0e0;'>"
                +
                "<div style='background-color: #FFD700; padding: 20px 20px; text-align: center; color: #333333;'>"
                +
                "<h1 style='margin: 0; font-size: 24px; font-weight: 600;'>[YenaNow] 이메일 인증</h1>" +
                "</div>" +
                "<div style='padding: 30px; text-align: center; color: #555555;'>" +
                "<p style='font-size: 16px; line-height: 1.6;'>안녕하세요. YenaNow에 오신 것을 환영합니다.</p>" +
                "<p style='font-size: 16px; line-height: 1.6;'>아래의 인증 코드를 입력하여 이메일 인증을 완료해 주세요.</p>"
                +
                "<div style='margin: 30px 0;'>" +
                "<span style='display: inline-block; padding: 15px 30px; background-color: #FFD700; color: #333333; font-size: 32px; font-weight: bold; letter-spacing: 5px; border-radius: 8px;'>"
                +
                "%s" +
                "</span>" +
                "</div>" +
                "<p style='font-size: 14px; color: #888888; margin-top: 20px;'>인증 코드는 <strong>%d분</strong> 동안 유효합니다.</p>"
                +
                "</div>" +
                "<div style='background-color: #f4f4f4; padding: 20px; text-align: center; color: #888888; font-size: 12px; border-top: 1px solid #e0e0e0;'>"
                +
                "<p style='margin: 0;'>본 메일은 발신 전용입니다. 문의사항은 웹사이트를 이용해 주세요.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>",
            code,
            VERIFICATION_CODE_TTL_MINUTES
        );

        mailService.sendEmail(email, subject, htmlContent, true);
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
        User user = userRepository.findByEmailAndProviderIsNull(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));

        String key = "verified:" + email;
        String verified = redisTemplate.opsForValue().get(key);

        if (!"true".equals(verified)) {
            throw new BusinessException(ErrorCode.PERMISSION_DENIED);
        }

        String tempPassword = generatePassword(12);

        user.setPassword(encoder.encode(tempPassword));
        userRepository.save(user);

        String subject = "[YenaNow] 임시 비밀번호 발급 안내";

        String htmlContent = String.format(
            "<!DOCTYPE html>" +
                "<html lang='ko'>" +
                "<head>" +
                "<meta charset='UTF-8'>" +
                "<meta name='viewport' content='width=device-width, initial-scale=1.0'>" +
                "<title>임시 비밀번호 발급</title>" +
                "</head>" +
                "<body style='font-family: -apple-system, BlinkMacSystemFont, \"Segoe UI\", Roboto, Oxygen, Ubuntu, Cantarell, \"Open Sans\", \"Helvetica Neue\", sans-serif; background-color: #f4f4f4; margin: 0; padding: 0;'>"
                +
                "<div style='max-width: 600px; margin: 20px auto; background-color: #ffffff; border-radius: 8px; overflow: hidden; box-shadow: 0 4px 8px rgba(0,0,0,0.05); border: 1px solid #e0e0e0;'>"
                +
                "<div style='background-color: #FFD700; padding: 20px 20px; text-align: center; color: #333333;'>"
                +
                "<h1 style='margin: 0; font-size: 24px; font-weight: 600;'>[YenaNow] 임시 비밀번호 발급 안내</h1>"
                +
                "</div>" +
                "<div style='padding: 30px; text-align: center; color: #555555;'>" +
                "<p style='font-size: 16px; line-height: 1.6;'>안녕하세요. YenaNow 임시 비밀번호 발급 안내입니다.</p>"
                +
                "<p style='font-size: 16px; line-height: 1.6;'>아래의 임시 비밀번호로 로그인 후 반드시 비밀번호를 변경해주세요.</p>"
                +
                "<div style='margin: 30px 0;'>" +
                "<span style='display: inline-block; padding: 15px 30px; background-color: #FFD700; color: #333333; font-size: 32px; font-weight: bold; letter-spacing: 5px; border-radius: 8px;'>"
                +
                "%s" + // 여기에 임시 비밀번호가 들어갑니다.
                "</span>" +
                "</div>" +
                "</div>" +
                "<div style='background-color: #f4f4f4; padding: 20px; text-align: center; color: #888888; font-size: 12px; border-top: 1px solid #e0e0e0;'>"
                +
                "<p style='margin: 0;'>본 메일은 발신 전용입니다. 문의사항은 웹사이트를 이용해 주세요.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>",
            tempPassword
        );

        mailService.sendEmail(email, subject, htmlContent, true);

        redisTemplate.delete(key);
    }

    @Override
    public String reissueToken(HttpServletRequest request) {
        String refreshToken = CookieUtil.getCookieValue(request, "refresh_token");

        if (refreshToken == null) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        try {
            jwtUtil.validateToken(refreshToken);

            String userUuid = jwtUtil.getSubject(refreshToken);
            String storedRefreshToken = redisTemplate.opsForValue()
                .get("refresh_token:" + userUuid);

            if (!refreshToken.equals(storedRefreshToken)) {
                throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
            }

            return jwtUtil.generateToken(userUuid);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
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