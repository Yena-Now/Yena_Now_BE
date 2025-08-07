package com.example.yenanow.users.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.smtp.MailService;
import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.common.util.UuidUtil;
import com.example.yenanow.users.dto.request.NicknameRequest;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.request.UpdateMyInfoRequest;
import com.example.yenanow.users.dto.request.UpdatePasswordRequest;
import com.example.yenanow.users.dto.response.MyInfoResponse;
import com.example.yenanow.users.dto.response.NicknameResponse;
import com.example.yenanow.users.dto.response.ProfileResponse;
import com.example.yenanow.users.dto.response.SignupResponse;
import com.example.yenanow.users.dto.response.UpdateProfileUrlResponse;
import com.example.yenanow.users.dto.response.UserSearchResponse;
import com.example.yenanow.users.dto.response.UserSearchResponseItem;
import com.example.yenanow.users.entity.Gender;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.FollowRepository;
import com.example.yenanow.users.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FollowRepository followRepository;
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
    public void updatePassword(UpdatePasswordRequest request, String userUuid) {
        String oldPassword = request.getOldPassword();
        String newPassword = request.getNewPassword();

        User user = UuidUtil.getUserByUuid(userRepository, userUuid);

        if (!encoder.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(
                ErrorCode.INVALID_PASSWORD);
        }

        // 새 비밀번호와 현재 비밀번호가 같으면
        if (encoder.matches(newPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST); // 400
        }

        // 비밀번호의 길이가 8자리 이상 16자리 이하가 아니면
        if (newPassword.length() < 8 || newPassword.length() > 16) {
            throw new BusinessException(ErrorCode.BAD_REQUEST); // 400
        }

        user.setPassword(encoder.encode(newPassword));
    }

    @Override
    public MyInfoResponse getMyInfo(String userUuid) {
        User user = UuidUtil.getUserByUuid(userRepository, userUuid);

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
    public void updateMyInfo(UpdateMyInfoRequest request, String userUuid) {
        User user = UuidUtil.getUserByUuid(userRepository, userUuid);

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getGender() != null) {
            user.setGender(Gender.from(request.getGender()));
        }

        // 빈 문자열인 경우 기존 값 삭제
        if (request.getBirthdate() != null) {
            if (request.getBirthdate().isEmpty()) {
                user.setBirthdate(null);
            } else {
                try {
                    user.setBirthdate(LocalDate.parse(request.getBirthdate()));
                } catch (DateTimeParseException e) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST);
                }
            }
        }

        // 빈 문자열인 경우 기존 값 삭제
        if (request.getPhoneNumber() != null) {
            if (request.getPhoneNumber().isEmpty()) {
                user.setPhoneNumber(null);
            } else {
                boolean isValid = request.getPhoneNumber()
                    .matches("^01(?:0|1|[6-9])-(?:\\d{3}|\\d{4})-\\d{4}$");
                if (!isValid) {
                    throw new BusinessException(ErrorCode.BAD_REQUEST);
                }
                user.setPhoneNumber(request.getPhoneNumber());
            }
        }
    }

    @Transactional
    @Override
    public void deleteMyInfo(String userUuid) {
        User user = UuidUtil.getUserByUuid(userRepository, userUuid);

        redisTemplate.delete("user:" + userUuid);
        redisTemplate.delete("refresh_token:" + userUuid);

        userRepository.delete(user);
    }

    @Override
    public ProfileResponse getProfile(String userUuid) {
        UuidUtil.validateUuid(userUuid);
        User toUser = UuidUtil.getUserByUuid(userRepository, userUuid);

        String currentUserUuid = SecurityContextHolder.getContext().getAuthentication().getName();
        User fromUser = UuidUtil.getUserByUuid(userRepository, currentUserUuid);

        boolean isFollowing = followRepository.existsByFromUserAndToUser(fromUser, toUser);
        boolean isMine = userUuid.equals(currentUserUuid);

        return ProfileResponse.builder()
            .name(toUser.getName())
            .nickname(toUser.getNickname())
            .gender(toUser.getGender())
            .profileUrl(toUser.getProfileUrl())
            .followingCount(toUser.getFollowingCount())
            .followerCount(toUser.getFollowerCount())
            .totalCut(toUser.getTotalCut())
            .isFollowing(isFollowing)
            .isMine(isMine)
            .build();
    }

    @Override
    public UserSearchResponse getUserSearch(String keyword, String currentUserUuid,
        int pageNum, int display) {
        Pageable pageable = PageRequest.of(pageNum, display);
        Page<UserSearchResponseItem> page = userRepository
            .findUsersByKeyword(currentUserUuid, keyword, pageable);

        return UserSearchResponse.builder()
            .totalPages(page.getTotalPages())
            .userSearches(page.getContent())
            .build();
    }

    @Transactional
    @Override
    public UpdateProfileUrlResponse updateProfileUrl(String userUuid,
        String imageUrl) {
        User user = UuidUtil.getUserByUuid(userRepository, userUuid);
        user.setProfileUrl(imageUrl);

        return UpdateProfileUrlResponse.builder()
            .imageUrl(user.getProfileUrl())
            .build();
    }

    @Transactional
    @Override
    public void deleteProfileUrl(String userUuid) {
        User user = UuidUtil.getUserByUuid(userRepository, userUuid);

        user.setProfileUrl(null);
    }
}