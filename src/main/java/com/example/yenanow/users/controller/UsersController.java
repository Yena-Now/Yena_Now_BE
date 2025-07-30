package com.example.yenanow.users.controller;

import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
import com.example.yenanow.users.dto.request.ModifyMyInfoRequest;
import com.example.yenanow.users.dto.request.ModifyPasswordRequest;
import com.example.yenanow.users.dto.request.NicknameRequest;
import com.example.yenanow.users.dto.request.SignupRequest;
import com.example.yenanow.users.dto.response.MyInfoResponse;
import com.example.yenanow.users.dto.response.NicknameResponse;
import com.example.yenanow.users.dto.response.SignupResponse;
import com.example.yenanow.users.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Users", description = "사용자 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UsersController {

    private final UserService userService;

    @Operation(summary = "자체 회원가입", description = "이메일과 비밀번호를 이용해 회원가입을 수행합니다.")
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest signupRequest) {
        return ResponseEntity.ok(userService.createUser(signupRequest));
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
    public ResponseEntity<Void> modifyPassword(@RequestBody ModifyPasswordRequest request) {
        userService.modifyPassword(request);
        return ResponseEntity.noContent().build(); // 204
    }

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 확인합니다.")
    @GetMapping("/me")
    public ResponseEntity<MyInfoResponse> getMyInfo() {
        return ResponseEntity.ok(userService.getMyInfo());
    }

    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 정보를 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<MyInfoResponse> modifyMyInfo(@RequestBody ModifyMyInfoRequest request) {
        userService.modifyMyInfo(request);
        return ResponseEntity.noContent().build(); // 204;
    }
}