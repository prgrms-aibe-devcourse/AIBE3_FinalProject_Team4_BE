package com.back.domain.user.user.service;

import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.bookmark.repository.BlogBookmarkRepository;
import com.back.domain.blog.like.repository.BlogLikeRepository;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogbookmark.repository.ShorlogBookmarkRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import com.back.domain.user.follow.repository.FollowRepository;
import com.back.domain.user.follow.service.FollowService;
import com.back.domain.user.user.dto.*;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.exception.UserErrorCase;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ShorlogRepository shorlogRepository;
    private final ShorlogLikeRepository shorlogLikeRepository;
    private final ShorlogBookmarkRepository shorlogBookmarkRepository;
    private final BlogRepository blogRepository;
    private final BlogLikeRepository blogLikeRepository;
    private final BlogBookmarkRepository blogBookmarkRepository;
    private final FollowRepository followRepository;
    private final FollowService followService;

    @Transactional(readOnly = true)

    public List<UserListResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    long followersCount = followService.countFollowers(user.getId());
                    return new UserListResponseDto(user, followersCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfileResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        long followersCount = followService.countFollowers(userId);
        long followingCount = followService.countFollowings(userId);
        long bloglikesCount = blogLikeRepository.countAllByUserId(userId);
        long shorlogLikesCount = shorlogLikeRepository.countAllByUserId(userId);
        long likesCount = bloglikesCount + shorlogLikesCount;
        int shorlogsCount = shorlogRepository.countAllByUserId(userId);
        int blogsCount = blogRepository.countAllByUserId(userId);

        return new ProfileResponseDto(user, followersCount, followingCount, likesCount, shorlogsCount, blogsCount);
    }

    @Transactional(readOnly = true)
    public MyProfileResponseDto getMyUser(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        long followersCount = followService.countFollowers(userId);
        long followingCount = followService.countFollowings(userId);
        long bloglikesCount = blogLikeRepository.countAllByUserId(userId);
        long shorlogLikesCount = shorlogLikeRepository.countAllByUserId(userId);
        long likesCount = bloglikesCount + shorlogLikesCount;
        int shorlogsCount = shorlogRepository.countAllByUserId(userId);
        int blogsCount = blogRepository.countAllByUserId(userId);
        int shorlogBookmarksCount = shorlogBookmarkRepository.countAllByUserId(userId);
        int blogBookmarksCount = blogBookmarkRepository.countAllByUserId(userId);

        return new MyProfileResponseDto(user, followersCount, followingCount, likesCount, shorlogsCount, blogsCount, shorlogBookmarksCount, blogBookmarksCount);
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequestDto dto) {
        if (!isAvailableNickname(dto.nickname())) {
            throw new ServiceException(UserErrorCase.NICKNAME_ALREADY_EXISTS);
        }

        User user = userRepository.findById(userId).
                orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        user.updateProfile(dto.nickname(), dto.bio(), dto.profileImgUrl());
        return new UserDto(user);
    }

    @Transactional(readOnly = true)
    public boolean isAvailableNickname(String nickname) {
        return userRepository.findByNickname(nickname).isEmpty();
    }

    @Transactional(readOnly = true)
    public List<UserListResponseDto> searchUserByKeyword(String keyword) {
        List<User> users = userRepository.findByNicknameContainingIgnoreCaseOrBioContainingIgnoreCaseOrderByFollowersCountDesc(keyword, keyword);

        return users.stream()
                .map(UserListResponseDto::new)
                .toList();
    }


    public List<CreatorListResponseDto> getCreators(Long userId) {
        List<User> allUsers = userRepository.findAll();
        List<Long> allUserIds = allUsers.stream()
                .map(User::getId)
                .toList();

        Set<Long> followingIds;
        if (userId == null) {
            followingIds = Collections.emptySet();
        } else {
            followingIds = new HashSet<>(followRepository.findFollowingIdsByUserId(userId));
        }

        System.out.println("followingIds = " + followingIds);
        // 4. 팔로워 많은 순으로 정렬
        return allUsers.stream()
                .map(user -> {
                    boolean isFollowing = followingIds.contains(user.getId());
                    return new CreatorListResponseDto(
                            user,
                            isFollowing
                    );
                })
                // 4. 팔로워 많은 순으로 정렬
                .sorted(Comparator.comparingLong(CreatorListResponseDto::followersCount).reversed())
                .toList();
    }

//    public List<FullCreatorListResponseDto> getCreatorsFull(Long viewerIdOrNull) {
//
//        List<User> allUsers = userRepository.findAll();
//        List<Long> allUserIds = allUsers.stream().map(User::getId).toList();
//
//        // ✔ 로그인 여부 분기
//        Set<Long> followingIds;
//        if (viewerIdOrNull == null) {
//            followingIds = Collections.emptySet(); // 모두 false
//        } else {
//            followingIds = new HashSet<>(
//                    followRepository.findFollowingIdsByUserId(viewerIdOrNull)
//            );
//        }
//
//        // followersCount bulk
//        List<Object[]> followerCounts =
//                followRepository.findFollowerCountsByUserIds(allUserIds);
//
//        Map<Long, Long> followersCountMap = followerCounts.stream()
//                .collect(Collectors.toMap(
//                        row -> (Long) row[0],
//                        row -> (Long) row[1]
//                ));
//
//        // 대표 썸네일 조회
//        Map<Long, String> userThumbnailMap = new HashMap<>();
//        for (Long targetUserId : allUserIds) {
//            Page<Shorlog> page = shorlogRepository.findByUserIdOrderByPopularity(
//                    targetUserId,
//                    PageRequest.of(0, 1)
//            );
//            if (!page.isEmpty() && !page.getContent().get(0).getImages().isEmpty()) {
//                userThumbnailMap.put(
//                        targetUserId,
//                        page.getContent().get(0).getImages().get(0).getImage().getS3Url()
//                );
//            } else {
//                userThumbnailMap.put(targetUserId, null);
//            }
//        }
//
//        // followersCount 순 정렬
//        List<User> sortedUsers = allUsers.stream()
//                .sorted((u1, u2) -> Long.compare(
//                        followersCountMap.getOrDefault(u2.getId(), 0L),
//                        followersCountMap.getOrDefault(u1.getId(), 0L)
//                ))
//                .toList();
//
//        // DTO 생성
//        return sortedUsers.stream()
//                .map(user -> new CreatorListResponseDto(
//                        user.getId(),
//                        user.getNickname(),
//                        user.getProfileImgUrl(),
//                        followersCountMap.getOrDefault(user.getId(), 0L),
//                        followingIds.contains(user.getId()),     // ✔ 로그인 여부에 따라 자동 처리
//                        userThumbnailMap.get(user.getId())
//                ))
//                .toList();
//    }

}
