package com.back.domain.user.user.controller;

import com.back.domain.blog.blog.dto.BlogDto;
import com.back.domain.blog.blog.entity.BlogMySortType;
import com.back.domain.blog.blog.service.BlogService;
import com.back.domain.blog.blogdoc.dto.BlogSliceResponse;
import com.back.domain.user.follow.service.FollowService;
import com.back.domain.user.user.dto.*;
import com.back.domain.user.user.service.UserService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 API")
public class UserController {

    private final UserService userService;
    private final BlogService blogService;
    private final FollowService followService; // ✅ 추가

    @GetMapping()
    @Operation(summary = "전체 유저 목록 조회")
    public RsData<List<UserListResponseDto>> getUsers() {
        List<UserListResponseDto> userDtos = userService.getAllUsers();
        return RsData.of("200", "유저 목록 조회 성공", userDtos);
    }

    @GetMapping("/{id}")
    @Operation(summary = "특정 유저 조회 (팔로우 관계 포함)")
    public RsData<ProfileResponseDto> getUserById(
            @PathVariable Long id,
            @AuthenticationPrincipal SecurityUser viewer // null 가능 (비로그인)
    ) {
        // 기본 프로필 데이터 (카운트 포함)
        ProfileResponseDto baseProfile = userService.getUserById(id);

        Long viewerId = (viewer != null) ? viewer.getId() : null;

        boolean isFollowing = false; // 내가 이 유저를 팔로우 중?
        boolean isFollower = false;  // 이 유저가 나를 팔로우 중?

        // 로그인했고, 자기 자신이 아닌 경우에만 팔로우 관계 계산
        if (viewerId != null && !viewerId.equals(id)) {
            isFollowing = followService.isFollowing(viewerId, id); // viewer -> target
            isFollower = followService.isFollowing(id, viewerId);  // target -> viewer
        }

        ProfileResponseDto enriched =
                ProfileResponseDto.withFollowStatus(baseProfile, isFollowing, isFollower);

        return RsData.of("200", "유저 조회 성공", enriched);
    }

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회")
    public RsData<MyProfileResponseDto> getMyProfile(
            @AuthenticationPrincipal SecurityUser user
    ) {
        MyProfileResponseDto myProfileResponseDto = userService.getMyUser(user.getId());
        return RsData.of("200", "내 프로필 조회 성공", myProfileResponseDto);
    }

    @PutMapping("/update")
    @Operation(summary = "내 프로필 수정")
    public RsData<UserDto> updateMyProfile(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody UpdateProfileRequestDto dto
    ) {
        UserDto userDto = userService.updateProfile(user.getId(), dto);
        return RsData.of("200", "프로필 수정 성공", userDto);
    }

    @GetMapping("/check-nickname")
    @Operation(summary = "닉네임 중복 확인")
    public RsData<Void> checkNicknameAvailable(@RequestParam String nickname) {
        boolean result = userService.isAvailableNickname(nickname);
        if (result) {
            return RsData.of("200", "사용 가능한 닉네임입니다.", null);
        } else {
            return RsData.of("409", "이미 사용 중인 닉네임입니다.", null);
        }
    }

    @GetMapping("/search")
    @Operation(summary = "유저 검색")
    public RsData<List<UserListResponseDto>> searchUser(@Valid @RequestParam String keyword) {
        List<UserListResponseDto> users = userService.searchUserByKeyword(keyword);
        return RsData.of("200", "유저 검색 성공", users);
    }

    @GetMapping("/creators")
    @Operation(summary = "크리에이터 목록 조회")
    public RsData<List<CreatorListResponseDto>> getCreators(
            @AuthenticationPrincipal SecurityUser user
    ) {
        Long userId = user != null ? user.getId() : null;
        List<CreatorListResponseDto> creators = userService.getCreators(userId);
        return RsData.of("200", "크리에이터 목록 조회 성공", creators);
    }

    @GetMapping("/{userId}/blogs")
    @Operation(summary = "유저별 블로그 글 다건 조회")
    public BlogSliceResponse<BlogDto> getUserItems(
            @AuthenticationPrincipal SecurityUser userDetails,
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "LATEST") BlogMySortType sortType
    ) {
        Long viewerId = userDetails != null ? userDetails.getId() : null;
        PageRequest pageable = PageRequest.of(page, size);
        Page<BlogDto> result = blogService.findAllByUserId(userId, viewerId, sortType, pageable);
        boolean hasNext = result.hasNext();
        String nextCursor = hasNext ? String.valueOf(result.getNumber() + 1) : null;
        return new BlogSliceResponse<>(result.getContent(), hasNext, nextCursor);
    }
}
