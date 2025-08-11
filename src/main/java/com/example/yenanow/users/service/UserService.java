package com.example.yenanow.users.service;

import com.example.yenanow.common.smtp.request.VerificationEmailRequest;
import com.example.yenanow.common.smtp.request.VerifyEmailRequest;
import com.example.yenanow.common.smtp.response.VerifyEmailResponse;
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
import com.example.yenanow.users.dto.response.UserSearchResponse;

public interface UserService {

    SignupResponse createUser(SignupRequest signupRequest);

    NicknameResponse validateNickname(NicknameRequest nicknameRequest);

    void sendMessage(VerificationEmailRequest request);

    VerifyEmailResponse verifyMessage(VerifyEmailRequest request);

    void updatePassword(UpdatePasswordRequest request, String userUuid);

    MyInfoResponse getMyInfo(String userUuid);

    void updateMyInfo(UpdateMyInfoRequest request, String userUuid);

    void deleteMyInfo(String userUuid);

    ProfileResponse getProfile(String userUuid);

    UserSearchResponse getUserSearch(String keyword, String currentUserUuid, int pageNum,
        int display);

    UserInviteSearchResponse getUserInviteSearch(String keyword, String currentUserUuid,
        int pageNum, int display);

    UpdateProfileUrlResponse updateProfileUrl(String userUuid, String imageUrl);

    void deleteProfileUrl(String userUuid);
}