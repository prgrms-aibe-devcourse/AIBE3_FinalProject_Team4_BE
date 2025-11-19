package com.back.domain.user.follow.service;

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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public Follow follow(Long followerId, Long followingId) {
        if(followerId.equals(followingId)) {
            throw new ServiceException(FollowErrorCase.CANNOT_FOLLOW_YOURSELF);
        }

        User follower = userRepository.findById(followerId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));
        User following = userRepository.findById(followingId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        if(isFollowing(followerId, followingId)) {
            throw new ServiceException(FollowErrorCase.ALREADY_FOLLOWING);
        }

        Follow follow = Follow.create(follower, following);
        return followRepository.save(follow);
    }

    @Transactional
    public void unfollow(Long followerId, Long followingId) {
        Follow follow = followRepository.findByFromUserIdAndToUserId(followerId, followingId)
                .orElseThrow(() -> new ServiceException(FollowErrorCase.NOT_EXISTING_FOLLOW));
        followRepository.delete(follow);
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        userRepository.findById(followingId).orElseThrow(
                () -> new ServiceException(UserErrorCase.USER_NOT_FOUND)
        );
        return followRepository.existsByFromUserIdAndToUserId(followerId, followingId);
    }

    @Transactional(readOnly = true)
    public List<FollowResponseDto> getFollowers(Long userId) {
        Set<Long> followingIds = new HashSet<>(followRepository.findFollowingIdsByUserId(userId));
        List<Long> followerIds = followRepository.findFollowerIdsByUserId(userId);
        return followerIds.stream()
                .map(followerId -> {    // 각 Follow 데이터 돌면서
                    User toUser = userRepository.findById(followerId)
                            .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));
                    return new FollowResponseDto(toUser, followingIds.contains(followerId));
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FollowResponseDto> getFollowings(Long userId) {
        List<Long> followingIds = followRepository.findFollowingIdsByUserId(userId);
        return followingIds.stream()
                .map(followingId -> {
                    User toUser = userRepository.findById(followingId)
                            .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));
                    return new FollowResponseDto(toUser, true);
                })
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
}
