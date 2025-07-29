package com.example.yenanow.users.service;

import com.example.yenanow.common.smtp.MailService;
import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.dto.request.NicknameRequest;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.response.NicknameResponse;
import com.example.yenanow.users.dto.response.SignupResponse;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;
    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;

    private static final long VERIFICATION_CODE_TTL_MINUTES = 5;

    @Override
    public SignupResponse createUser(SignupRequest signupRequest) {
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

    @Override
    public NicknameResponse validateNickname(NicknameRequest nicknameRequest) {
        String nickname = nicknameRequest.getNickname();
        boolean isDuplicated = userRepository.existsByNickname(nickname);

        return new NicknameResponse(isDuplicated);
    }

    @Override
    public void sendVerification(VerificationEmailRequest request) {
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) { // 이메일 중복 검사
            throw new RuntimeException("이미 가입된 이메일입니다.");
        }

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
            redisTemplate.delete(key); // 인증 성공 시 Redis에서 삭제
        }

        return new VerifyEmailResponse(isVerified);
    }
}
