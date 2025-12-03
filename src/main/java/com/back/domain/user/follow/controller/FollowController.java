package com.back.domain.user.follow.controller;

import com.back.domain.user.follow.dto.UserProfileWithFollowStatusResponseDto;
import com.back.domain.user.follow.dto.FollowCountResponseDto;
import com.back.domain.user.follow.dto.FollowResponseDto;
import com.back.domain.user.follow.entity.Follow;
import com.back.domain.user.follow.service.FollowService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/follow")
@Tag(name = "Follow API", description = "팔로우 관련 API")
public class FollowController {
    private final FollowService followService;

    @PostMapping("/{followingId}")
    @Operation(summary = "팔로우 하기")
    public RsData<Void> follow(@Valid @PathVariable Long followingId, @AuthenticationPrincipal SecurityUser user) {
        Follow follow = followService.follow(user.getId(), followingId);
        return RsData.of("200", "팔로우 성공", null);
    }

    @DeleteMapping("/{unfollowingId}")
    @Operation(summary = "팔로우 취소 하기")
    public RsData<Void> unfollow(@Valid @PathVariable Long unfollowingId, @AuthenticationPrincipal SecurityUser user) {
        followService.unfollow(user.getId(), unfollowingId);
        return RsData.of("200", "팔로우 취소 성공", null);
    }

    @GetMapping("/is-following/{followingId}")
    @Operation(summary = "특정 유저 팔로우 여부 확인")
    public RsData<Void> isFollowing(@Valid @PathVariable Long followingId, @AuthenticationPrincipal SecurityUser user) {
        boolean isFollowing = followService.isFollowing(user.getId(), followingId);
        if(isFollowing) {
            return RsData.of("200", "팔로우 중입니다.", null);
        } else {
            return RsData.of("200", "팔로우 중이 아닙니다.", null);
        }
    }

    @GetMapping("/followers/{userId}")
    @Operation(summary = "팔로워 목록 조회")
    public RsData<List<FollowResponseDto>> getFollowers(@Valid @PathVariable Long userId, @AuthenticationPrincipal SecurityUser user) {
        Long currentUserId = user != null ? user.getId() : null;
        List<FollowResponseDto> followerResponseDto = followService.getFollowers(userId, currentUserId);
        return RsData.of("200", "팔로워 목록 조회 성공", followerResponseDto);
    }

    @GetMapping("/followings/{userId}")
    @Operation(summary = "팔로잉 목록 조회")
    public RsData<List<FollowResponseDto>> getFollowings(@Valid @PathVariable Long userId, @AuthenticationPrincipal SecurityUser user) {
        Long currentUserId = user != null ? user.getId() : null;
        List<FollowResponseDto> followingResponseDto = followService.getFollowings(userId, currentUserId);
        return RsData.of("200", "팔로잉 목록 조회 성공", followingResponseDto);
    }

    @GetMapping("/counts/{userId}")
    @Operation(summary = "팔로워/팔로잉 카운트 조회")
    public RsData<FollowCountResponseDto> getFollowCounts(@Valid @PathVariable Long userId) {
        FollowCountResponseDto followCountResponseDto = followService.getFollowCounts(userId);
        return RsData.of("200", "팔로워/팔로잉 카운트 조회 성공", followCountResponseDto);
    }

    @GetMapping("/{targetUserId}/profile")
    @Operation(summary = "프로필 + 팔로우 상태 조회")
    public RsData<UserProfileWithFollowStatusResponseDto> getUserProfileWithFollowStatus(
            @AuthenticationPrincipal SecurityUser viewer,
            @PathVariable Long targetUserId
    ) {
        return RsData.successOf(
                followService.getUserProfileWithFollowStatus(viewer.getId(), targetUserId)
        );
    }
}
