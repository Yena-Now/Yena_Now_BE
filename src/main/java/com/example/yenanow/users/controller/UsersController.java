package com.example.yenanow.users.controller;

import com.example.yenanow.common.exception.BusinessException;
import com.example.yenanow.common.exception.ErrorCode;
import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import com.example.yenanow.s3.service.S3Service;
import com.example.yenanow.s3.util.S3KeyFactory;
import com.example.yenanow.users.dto.request.NicknameRequest;
import com.example.yenanow.users.dto.request.PresignedProfileRequest;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.request.UpdateMyInfoRequest;
import com.example.yenanow.users.dto.request.UpdatePasswordRequest;
import com.example.yenanow.users.dto.request.UpdateProfileUrlRequest;
import com.example.yenanow.users.dto.response.MyInfoResponse;
import com.example.yenanow.users.dto.response.NicknameResponse;
import com.example.yenanow.users.dto.response.ProfileResponse;
import com.example.yenanow.users.dto.response.SignupProfileUrlResponse;
import com.example.yenanow.users.dto.response.SignupResponse;
import com.example.yenanow.users.dto.response.UpdateProfileUrlResponse;
import com.example.yenanow.users.dto.response.UserSearchResponse;
import com.example.yenanow.users.service.FollowService;
import com.example.yenanow.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "사용자 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;
    private final FollowService followService;
    private final S3Service s3Service;
    private final S3KeyFactory s3KeyFactory;

    @Operation(summary = "자체 회원가입", description = "이메일과 비밀번호를 이용해 회원가입을 수행합니다.")
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(userService.createUser(signupRequest));
    }

    @Operation(summary = "회원가입용 프로필 Presigned URL 발급")
    @PostMapping("/signup/profile-url")
    public ResponseEntity<SignupProfileUrlResponse> presignProfile(
        @RequestBody PresignedProfileRequest req) {

        if (req == null) {
            throw new BusinessException(ErrorCode.EMPTY_REQUEST_BODY);
        }

        String key = s3KeyFactory.createTempProfileKey(req.getFileName());
        String uploadUrl = s3Service.generatePresignedUploadUrl(key, req.getContentType());
        String fileUrl = s3Service.getFileUrl(key);
        return ResponseEntity.ok(new SignupProfileUrlResponse(uploadUrl, fileUrl));
    }


    @Operation(summary = "닉네임 중복 확인", description = "입력한 닉네임이 이미 사용 중인지 확인합니다.")
    @PostMapping("/nickname")
    public ResponseEntity<NicknameResponse> validateNickname(
        @RequestBody NicknameRequest nicknameRequest) {
        return ResponseEntity.ok(userService.validateNickname(nicknameRequest));
    }

    @Operation(summary = "이메일 인증코드 발송", description = "사용자 이메일로 인증 코드를 전송합니다.")
    @PostMapping("/verification-email")
    public ResponseEntity<Void> sendMessage(@RequestBody VerificationEmailRequest request) {
        userService.sendMessage(request);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "이메일 인증 코드 확인", description = "이메일로 받은 인증 코드를 검증합니다.")
    @PostMapping("/verify-email")
    public ResponseEntity<VerifyEmailResponse> verifyMessage(
        @RequestBody VerifyEmailRequest request) {
        return ResponseEntity.ok(userService.verifyMessage(request));
    }

    @Operation(summary = "비밀번호 변경", description = "기존 비밀번호를 확인 후 비밀번호를 변경합니다.")
    @PatchMapping("/password")
    public ResponseEntity<Void> updatePassword(@AuthenticationPrincipal Object principal,
        @RequestBody UpdatePasswordRequest updatePasswordRequest) {
        String currentUserUuid = principal.toString();
        userService.updatePassword(updatePasswordRequest, currentUserUuid);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 확인합니다.")
    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> getMyInfo(@AuthenticationPrincipal Object principal) {
        String currentUserUuid = principal.toString();
        return ResponseEntity.ok(userService.getMyInfo(currentUserUuid));
    }

    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<MyInfoResponse> updateMyInfo(@AuthenticationPrincipal Object principal,
        @RequestBody UpdateMyInfoRequest request) {
        String currentUserUuid = principal.toString();
        userService.updateMyInfo(request, currentUserUuid);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다.")
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteMyInfo(@AuthenticationPrincipal Object principal) {
        String currentUserUuid = principal.toString();
        userService.deleteMyInfo(currentUserUuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "사용자 프로필 조회", description = "사용자의 프로필을 조회합니다.")
    @GetMapping("/profile/{userUuid}")
    public ResponseEntity<ProfileResponse> getMyProfile(@PathVariable String userUuid) {
        return ResponseEntity.ok(userService.getProfile(userUuid));
    }

    @Operation(summary = "사용자 검색", description = "사용자를 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<UserSearchResponse> getUserSearch(
        @AuthenticationPrincipal Object principal,
        @RequestParam("keyword") String keyword,
        @RequestParam("pageNum") int pageNum,
        @RequestParam("display") int display) {
        String currentUserUuid = principal.toString();
        return ResponseEntity.ok(
            userService.getUserSearch(keyword, currentUserUuid, pageNum, display));
    }

    @Operation(summary = "프로필 사진 등록", description = "프로필 사진을 등록합니다.")
    @PatchMapping("/image")
    public ResponseEntity<UpdateProfileUrlResponse> updateProfileUrl(
        @AuthenticationPrincipal Object principal, @RequestBody UpdateProfileUrlRequest request) {
        String currentUserUuid = principal.toString();
        String imageUrl = request.getImageUrl();
        return ResponseEntity.ok(userService.updateProfileUrl(currentUserUuid, imageUrl));
    }

    @Operation(summary = "프로필 사진 삭제", description = "프로필 사진을 삭제합니다.")
    @DeleteMapping("/image")
    public ResponseEntity<Void> deleteProfileUrl(@AuthenticationPrincipal Object principal) {
        String currentUserUuid = principal.toString();
        userService.deleteProfileUrl(currentUserUuid);
        return ResponseEntity.noContent().build();
    }
}