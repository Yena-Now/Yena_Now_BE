package com.example.yenanow.users.controller;

import com.example.yenanow.users.dto.response.FollowerResponse;
import com.example.yenanow.users.dto.response.FollowingResponse;
import com.example.yenanow.users.service.FollowService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Follow", description = "사용자 팔로우 관련 API")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "특정 유저 팔로우", description = "로그인한 사용자가 지정한 userUuid 사용자를 팔로우합니다.")
    @PostMapping("/{userUuid}/followers")
    public ResponseEntity<Void> followUser(
        @Parameter(description = "팔로우할 사용자 UUID", required = true)
        @PathVariable String userUuid,
        @Parameter(hidden = true)
        @AuthenticationPrincipal Object principal) {

        String currentUserUuid = principal.toString();
        followService.follow(currentUserUuid, userUuid);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "특정 유저 언팔로우", description = "로그인한 사용자가 지정한 userUuid 사용자를 언팔로우합니다.")
    @DeleteMapping("/{userUuid}/followers/me")
    public ResponseEntity<Void> unfollowUser(
        @Parameter(description = "언팔로우할 사용자 UUID", required = true)
        @PathVariable String userUuid,
        @Parameter(hidden = true)
        @AuthenticationPrincipal Object principal) {

        String currentUserUuid = principal.toString();
        followService.unfollow(currentUserUuid, userUuid);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "팔로우 여부 확인", description = "로그인한 사용자가 지정한 userUuid 사용자를 팔로우했는지 여부를 확인합니다.")
    @GetMapping("/{userUuid}/followers/check")
    public ResponseEntity<Boolean> isFollowing(
        @Parameter(description = "팔로우 여부 확인할 사용자 UUID", required = true)
        @PathVariable String userUuid,
        @Parameter(hidden = true)
        @AuthenticationPrincipal Object principal) {

        String currentUserUuid = principal.toString();
        boolean result = followService.isFollowing(currentUserUuid, userUuid);
        return ResponseEntity.ok(result);
    }
    
    @Operation(summary = "타인 팔로잉 목록 조회", description = "사용자의 팔로잉 목록을 조회합니다.")
    @GetMapping("/{userUuid}/followings")
    public ResponseEntity<FollowingResponse> getFollowings(
        @AuthenticationPrincipal Object principal,
        @PathVariable String userUuid,
        @RequestParam int pageNum,
        @RequestParam int display) {
        String currentUserUuid = principal.toString();
        return ResponseEntity.ok(
            followService.getFollowings(userUuid, currentUserUuid, pageNum, display));
    }

    @Operation(summary = "타인 팔로워 목록 조회", description = "사용자의 팔로워 목록을 조회합니다.")
    @GetMapping("/{userUuid}/followers")
    public ResponseEntity<FollowerResponse> getFollowers(
        @AuthenticationPrincipal Object principal,
        @PathVariable String userUuid,
        @RequestParam int pageNum,
        @RequestParam int display) {
        String currentUserUuid = principal.toString();
        return ResponseEntity.ok(
            followService.getFollowers(userUuid, currentUserUuid, pageNum, display));
    }
}
