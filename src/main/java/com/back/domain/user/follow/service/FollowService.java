package com.back.domain.user.follow.service;

import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.follow.dto.FollowCountResponseDto;
import com.back.domain.user.follow.dto.FollowResponseDto;
import com.back.domain.user.follow.entity.Follow;
import com.back.domain.user.follow.exception.FollowErrorCase;
import com.back.domain.user.follow.repository.FollowRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.exception.UserErrorCase;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    private final NotificationService notificationService;   //알림 서비스 주입

    @Transactional
    public Follow follow(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new ServiceException(FollowErrorCase.CANNOT_FOLLOW_YOURSELF);
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        if (isFollowing(followerId, followingId)) {
            throw new ServiceException(FollowErrorCase.ALREADY_FOLLOWING);
        }

        follower.increaseFollowingCount();
        following.increaseFollowerCount();

        Follow follow = Follow.create(follower, following);
        followRepository.save(follow);

        // 알림 전송
        notificationService.send(
                followingId,                    // 알림 받을 사람
                followerId,                     // 알림 보낸 사람
                NotificationType.FOLLOW,        // 타입
                followerId,                     // targetId: 누가 팔로우했는지를 링크하거나 상세페이지 id
                follower.getNickname()          // 메시지 생성용 닉네임
        );

        return follow;
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFromUserIdAndToUserId(followerId, followingId)
                .orElseThrow(() -> new ServiceException(FollowErrorCase.NOT_EXISTING_FOLLOW));

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        follower.decreaseFollowingCount();
        following.decreaseFollowerCount();

        followRepository.delete(follow);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        return followRepository.existsByFromUserIdAndToUserId(followerId, followingId);
    }

    @Transactional(readOnly = true)
    public List<FollowResponseDto> getFollowers(Long userId, Long currentUserId) {
        // 대상의 팔로워들
        List<User> followers = followRepository.findFollowersByToUserId(userId);
        // 내가 팔로우하는 사람들
        Set<Long> myFollowingIds =
                (currentUserId == null) ? Collections.emptySet() :
                new HashSet<>(followRepository.findFollowingIdsByUserId(currentUserId));

        return followers.stream()
                .map(follower -> new FollowResponseDto(
                        follower,
                        myFollowingIds.contains(follower.getId())
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowResponseDto> getFollowings(Long userId, Long currentUserId) {
        //대상이 팔로우하는 사람들
        List<User> followings = followRepository.findFollowingsByFromUserId(userId);
        // 내가 팔로우하는 사람들
        Set<Long> myFollowingIds =
                (currentUserId == null) ? Collections.emptySet() :
                        new HashSet<>(followRepository.findFollowingIdsByUserId(currentUserId));

        return followings.stream()
                .map(following -> new FollowResponseDto(
                        following,
                        myFollowingIds.contains(following.getId())
                ))
                .toList();
    }

    public long countFollowers(Long userId) {
        return followRepository.countByToUserId(userId);
    }

    public long countFollowings(Long userId) {
        return followRepository.countByFromUserId(userId);
    }

    @Transactional(readOnly = true)
    public FollowCountResponseDto getFollowCounts(@Valid Long userId) {
        long followersCount = followRepository.countByToUserId(userId);
        long followingsCount = followRepository.countByFromUserId(userId);
        return new FollowCountResponseDto(followersCount, followingsCount);
    }

    public List<Long> findFollowingUserIds(Long id) {
        return followRepository.findFollowingIdsByUserId(id);
    }
}
