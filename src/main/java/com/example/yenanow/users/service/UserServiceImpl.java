package com.example.yenanow.users.service;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.smtp.MailService;
import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import com.example.yenanow.common.util.JwtUtil;
import com.example.yenanow.common.util.UuidUtil;
import com.example.yenanow.s3.service.S3Service;
import com.example.yenanow.s3.util.S3KeyFactory;
import com.example.yenanow.users.dto.request.NicknameRequest;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.request.UpdateMyInfoRequest;
import com.example.yenanow.users.dto.request.UpdatePasswordRequest;
import com.example.yenanow.users.dto.response.MyInfoResponse;
import com.example.yenanow.users.dto.response.NicknameResponse;
import com.example.yenanow.users.dto.response.ProfileResponse;
import com.example.yenanow.users.dto.response.SignupResponse;
import com.example.yenanow.users.dto.response.UpdateProfileUrlResponse;
import com.example.yenanow.users.dto.response.UserInviteSearchResponse;
import com.example.yenanow.users.dto.response.UserInviteSearchResponseItem;
import com.example.yenanow.users.dto.response.UserSearchResponse;
import com.example.yenanow.users.dto.response.UserSearchResponseItem;
import com.example.yenanow.users.entity.Gender;
import com.example.yenanow.users.entity.User;
import com.example.yenanow.users.repository.FollowRepository;
import com.example.yenanow.users.repository.UserRepository;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
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

    private static final long VERIFICATION_CODE_TTL_MINUTES = 5;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;
    private final JwtUtil jwtUtil;
    private final BCryptPasswordEncoder encoder;
    private final StringRedisTemplate redisTemplate;
    private final MailService mailService;
    private final S3KeyFactory s3KeyFactory;
    private final S3Service s3Service;

    @Transactional
    @Override
    public SignupResponse createUser(SignupRequest signupRequest) {

        if (userRepository.existsByNickname(signupRequest.getNickname())) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS_NICKNAME);
        }

        /* 1) 프로필 업로드 여부 판별 */
        boolean hasProfile =
            signupRequest.getProfileUrl() != null && !signupRequest.getProfileUrl().isBlank();

        /* 2) 최종 프로필 키 */
        String finalKey = null;   // 사진이 없으면 그대로 null

        if (hasProfile) {
            /* 2-a) temp URL → Key */
            String tempKey = s3KeyFactory.extractKeyFromUrl(signupRequest.getProfileUrl());

            /* 2-b) 확장자 추출 (png, jpg 등) */
            String ext = org.apache.commons.io.FilenameUtils.getExtension(tempKey);

            /* 2-c) 최종 Key: profile/{userUuid}/{랜덤}.{ext} */
            finalKey = s3KeyFactory.createProfileKeyWithoutUser(ext);

            /* 2-d) S3 복사 & temp 삭제 */
            s3Service.copyObject(tempKey, finalKey);
            s3Service.deleteObject(tempKey);
        }

        /* 3) 엔티티 생성 및 저장 */
        User user = signupRequest.toEntity();
        user.setProfileUrl(finalKey);      // null 또는 최종 Key
        user.encodePassword(encoder);
        userRepository.save(user);         // INSERT 한 번

        /* 4) 토큰 & Redis 초기화 */
        String userUuid = user.getUserUuid();
        String token = jwtUtil.generateToken(userUuid);
        String rKey = "user:" + userUuid;
        redisTemplate.opsForHash().put(rKey, "follower_count", "0");
        redisTemplate.opsForHash().put(rKey, "following_count", "0");
        redisTemplate.opsForHash().put(rKey, "total_cut", "0");

        /* 5) 응답: Key → URL 변환 (null 처리) */
        String profileUrl = finalKey == null ? null : s3Service.getFileUrl(finalKey);

        return SignupResponse.builder()
            .accessToken(token)
            .userUuid(userUuid)
            .nickname(user.getNickname())
            .profileUrl(profileUrl)   // null 이면 프런트에서 기본 아바타 사용
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

        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS);
        }

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
            .profileUrl(s3Service.getFileUrl(user.getProfileUrl()))
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
            .profileUrl(s3Service.getFileUrl(toUser.getProfileUrl()))
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

        List<UserSearchResponseItem> userSearches = page.getContent().stream()
            .map(item -> new UserSearchResponseItem(
                item.getUserUuid(),
                s3Service.getFileUrl(item.getProfileUrl()),
                item.getName(),
                item.getNickname(),
                item.getFollowing()
            ))
            .collect(Collectors.toList());

        return UserSearchResponse.builder()
            .totalPages(page.getTotalPages())
            .userSearches(userSearches)
            .build();
    }

    @Override
    public UserInviteSearchResponse getUserInviteSearch(String keyword, String currentUserUuid,
        int pageNum, int display) {
        Pageable pageable = PageRequest.of(pageNum, display);
        Page<UserInviteSearchResponseItem> page = userRepository
            .findFollowersByKeyword(currentUserUuid, keyword, pageable);

        List<UserInviteSearchResponseItem> userInviteSearches = page.getContent().stream()
            .map(item -> new UserInviteSearchResponseItem(
                item.getUserUuid(),
                s3Service.getFileUrl(item.getProfileUrl()),
                item.getName(),
                item.getNickname()
            ))
            .collect(Collectors.toList());

        return UserInviteSearchResponse.builder()
            .totalPages(page.getTotalPages())
            .userSearches(userInviteSearches)
            .build();
    }

    @Transactional
    @Override
    public UpdateProfileUrlResponse updateProfileUrl(String userUuid,
        String imageUrl) {

        User user = UuidUtil.getUserByUuid(userRepository, userUuid);

        /* 1) URL → Key 추출 */
        String key = s3KeyFactory.extractKeyFromUrl(imageUrl);   // profile/... 또는 profile/temp/...

        /* 2) DB에는 Key만 저장 */
        user.setProfileUrl(key);

        /* 3) 응답은 Key → URL 변환 */
        String finalUrl = s3Service.getFileUrl(key);

        return UpdateProfileUrlResponse.builder()
            .imageUrl(finalUrl)
            .build();
    }

    @Transactional
    @Override
    public void deleteProfileUrl(String userUuid) {
        User user = UuidUtil.getUserByUuid(userRepository, userUuid);

        user.setProfileUrl(null);
    }
}