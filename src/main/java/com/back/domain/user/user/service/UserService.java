package com.back.domain.user.user.service;

import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogdoc.service.BlogDocIndexer;
import com.back.domain.blog.bookmark.repository.BlogBookmarkRepository;
import com.back.domain.blog.like.repository.BlogLikeRepository;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogbookmark.repository.ShorlogBookmarkRepository;
import com.back.domain.shorlog.shorlogdoc.service.ShorlogDocService;
import com.back.domain.shorlog.shorlogimage.repository.ShorlogImagesRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import com.back.domain.user.follow.repository.FollowRepository;
import com.back.domain.user.follow.service.FollowService;
import com.back.domain.user.user.dto.*;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.exception.UserErrorCase;
import com.back.domain.user.user.file.ProfileImageService;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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
    private final ProfileImageService profileImageService;
    private final ShorlogImagesRepository shorlogImagesRepository;
    private final ShorlogDocService shorlogDocService;
    private final BlogDocIndexer blogDocIndexer;

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
        long bloglikesCount = blogLikeRepository.countByBlogUserId(userId);
        long shorlogLikesCount = shorlogLikeRepository.countByShorlogUserId(userId);
        long likesCount = bloglikesCount + shorlogLikesCount;
        int shorlogsCount = shorlogRepository.countAllByUserId(userId);
        int blogsCount = blogRepository.countAllByUserId(userId);

        log.info("shorlogsLikeCount: {}", shorlogLikesCount);
        log.info("blogLikesCount: {}", bloglikesCount);
        log.info("totalLikesCount: {}", likesCount);

        return new ProfileResponseDto(user, followersCount, followingCount, likesCount, shorlogsCount, blogsCount);
    }

    @Transactional(readOnly = true)
    public MyProfileResponseDto getMyUser(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        long followersCount = followService.countFollowers(userId);
        long followingCount = followService.countFollowings(userId);
        long bloglikesCount = blogLikeRepository.countByBlogUserId(userId);
        long shorlogLikesCount = shorlogLikeRepository.countByShorlogUserId(userId);
        long likesCount = bloglikesCount + shorlogLikesCount;
        int shorlogsCount = shorlogRepository.countAllByUserId(userId);
        int blogsCount = blogRepository.countAllByUserId(userId);
        int shorlogBookmarksCount = shorlogBookmarkRepository.countAllByUserId(userId);
        int blogBookmarksCount = blogBookmarkRepository.countAllByUserId(userId);

        log.info("shorlogsLikeCount: {}", shorlogLikesCount);
        log.info("blogLikesCount: {}", bloglikesCount);
        log.info("totalLikesCount: {}", likesCount);

        return new MyProfileResponseDto(user, followersCount, followingCount, likesCount, shorlogsCount, blogsCount, shorlogBookmarksCount, blogBookmarksCount);
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequestDto dto, MultipartFile profileImage) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        if (!dto.nickname().equals(user.getNickname())) {
            if (!isAvailableNickname(dto.nickname())) {
                throw new ServiceException(UserErrorCase.NICKNAME_ALREADY_EXISTS);
            }
        }

        String currentImgUrl = user.getProfileImgUrl();
        String updatedImageUrl = null;
        try {
            updatedImageUrl = profileImageService.updateFile(currentImgUrl, profileImage, dto.deleteExistingImage());
        } catch (IOException e) {
            throw new RuntimeException("파일 처리 중 오류가 발생했습니다.", e);
        }

        user.updateProfile(dto.nickname(), dto.bio(), updatedImageUrl);

        shorlogDocService.updateUserProfileInShorlogs(userId, dto.nickname(), updatedImageUrl);
        blogDocIndexer.updateUserProfileInBlogs(userId, dto.nickname(), updatedImageUrl);
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

        Set<Long> followingIds;
        if (userId == null) {
            followingIds = Collections.emptySet();
        } else {
            followingIds = new HashSet<>(followRepository.findFollowingIdsByUserId(userId));
        }

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

    public List<FullCreatorListResponseDto> getCreatorsFull(Long viewerIdOrNull) {

        List<User> allUsers = userRepository.findAll();
        Set<Long> creatorIds = new HashSet<>(shorlogRepository.findDistinctUserIdsWithAnyPost());
        allUsers = allUsers.stream()
                .filter(user -> creatorIds.contains(user.getId()))
                .toList();

        List<Long> allUserIds = allUsers.stream().map(User::getId).toList();

        // 로그인 여부 분기
        Set<Long> followingIds = (viewerIdOrNull == null)
                ? Collections.emptySet()
                : new HashSet<>(followRepository.findFollowingIdsByUserId(viewerIdOrNull));

        // followersCount bulk
        List<Object[]> followerCounts = followRepository.findFollowerCountsByUserIds(allUserIds);
        Map<Long, Long> followersCountMap = followerCounts.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));

        // ✅ 대표 썸네일 bulk (N+1 제거)
        Map<Long, String> userThumbnailMap = new HashMap<>();
        allUserIds.forEach(id -> userThumbnailMap.put(id, null)); // 기본 null 깔기

        List<Object[]> topRows = shorlogRepository.findTopShorlogIdByUserIdsOrderByPopularity(allUserIds);
        Map<Long, Long> userTopShorlogIdMap = topRows.stream()
                .collect(Collectors.toMap(
                        row -> ((Number) row[0]).longValue(),
                        row -> ((Number) row[1]).longValue()
                ));

        List<Long> topShorlogIds = new ArrayList<>(new HashSet<>(userTopShorlogIdMap.values()));
        if (!topShorlogIds.isEmpty()) {
            List<Object[]> thumbRows = shorlogImagesRepository.findFirstImageUrlByShorlogIds(topShorlogIds);
            Map<Long, String> shorlogIdToUrl = thumbRows.stream()
                    .collect(Collectors.toMap(
                            row -> ((Number) row[0]).longValue(),
                            row -> (String) row[1],
                            (a, b) -> a
                    ));

            userTopShorlogIdMap.forEach((userId, shorlogId) -> {
                userThumbnailMap.put(userId, shorlogIdToUrl.get(shorlogId)); // 없으면 null 유지
            });
        }

        // followersCount 순 정렬
        List<User> sortedUsers = allUsers.stream()
                .sorted((u1, u2) -> Long.compare(
                        followersCountMap.getOrDefault(u2.getId(), 0L),
                        followersCountMap.getOrDefault(u1.getId(), 0L)
                ))
                .toList();

        // DTO 생성
        return sortedUsers.stream()
                .map(user -> new FullCreatorListResponseDto(
                        user.getId(),
                        user.getNickname(),
                        user.getProfileImgUrl(),
                        followersCountMap.getOrDefault(user.getId(), 0L),
                        followingIds.contains(user.getId()),
                        userThumbnailMap.get(user.getId())
                ))
                .toList();
    }


}
