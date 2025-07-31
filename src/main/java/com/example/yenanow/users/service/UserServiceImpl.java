package com.example.yenanow.users.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.smtp.MailService;
import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.users.dto.request.ModifyMyInfoRequest;
import com.example.yenanow.users.dto.request.ModifyPasswordRequest;
import com.example.yenanow.users.dto.request.NicknameRequest;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.response.MyInfoResponse;
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
import org.springframework.transaction.annotation.Transactional;

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

        user = userRepository.save(user); // 저장 후 UUID 획득
        String token = jwtUtil.generateToken(user.getUserUuid());

        // Redis에 팔로워, 팔로잉 수 및 게시글(N컷) 수 초기값 0 저장
        String key = "user:" + user.getUserUuid();
        redisTemplate.opsForHash().put(key, "follower_count", "0");
        redisTemplate.opsForHash().put(key, "following_count", "0");
        redisTemplate.opsForHash().put(key, "total_cut", "0");

        return SignupResponse.builder()
            .accessToken(token)
            .userUuid(user.getUserUuid())
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
    public void sendMessage(VerificationEmailRequest request) {
        String email = request.getEmail();

        if (userRepository.existsByEmail(email)) { // 이메일 중복 검사
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
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
    public VerifyEmailResponse verifyMessage(VerifyEmailRequest request) {
        String email = request.getEmail();
        String key = "email:" + email;
        String code = redisTemplate.opsForValue().get(key);

        boolean isVerified = code != null && code.equals(request.getCode());

        if (isVerified) {
            redisTemplate.delete(key); // 인증 성공 시 Redis에서 삭제
        }

        return new VerifyEmailResponse(isVerified);
    }

    @Transactional
    @Override
    public void modifyPassword(ModifyPasswordRequest request, String userUuid) {
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        validateUuid(userUuid);
        User user = getUserByUuid(userUuid);

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(
                ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호와 현재 비밀번호가 같으면
        if (encoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST); // 400
        }

        user.setPassword(encoder.encode(newPassword));
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Override
    public MyInfoResponse getMyInfo(String userUuid) {
        validateUuid(userUuid);
        User user = getUserByUuid(userUuid);

        return MyInfoResponse.builder()
            .email(user.getEmail())
            .name(user.getName())
            .nickname(user.getNickname())
            .gender(user.getGender())
            .birthdate(user.getBirthdate())
            .phoneNumber(user.getPhoneNumber())
            .profileUrl(user.getProfileUrl())
            .build();
    }

    @Transactional
    @Override
    public void modifyMyInfo(ModifyMyInfoRequest request, String userUuid) {
        validateUuid(userUuid);
        User user = getUserByUuid(userUuid);

        String newName = request.getName();
        String newNickname = request.getNickname();
        String newPhoneNumber = request.getPhoneNumber();

        user.setName(newName);
        user.setNickname(newNickname);
        user.setPhoneNumber(newPhoneNumber);
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
    }

    @Transactional
    @Override
    public void deleteMyInfo(String userUuid) {
        validateUuid(userUuid);

        User user = getUserByUuid(userUuid);

        redisTemplate.delete("user:" + userUuid);
        redisTemplate.delete("refresh_token:" + userUuid);

        userRepository.delete(user);
    }

    /**
     * UUID 값 유효성 검증
     */
    private void validateUuid(String userUuid) {
        if (userUuid == null || userUuid.isBlank()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * UUID로 User 조회 (없으면 예외)
     */
    private User getUserByUuid(String userUuid) {
        return userRepository.findByUserUuid(userUuid)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_USER));
    }
}