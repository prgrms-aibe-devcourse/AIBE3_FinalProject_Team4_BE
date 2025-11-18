package com.back.domain.user.follow.service;

import com.back.domain.user.follow.entity.Follow;
import com.back.domain.user.follow.repository.FollowRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FollowService {
    private final FollowRepository followRepository;

    public void follow(Long followerId, Long followingId) {
        // Implement follow logic here
    }

    public void unfollow(Long followerId, Long followingId) {
        // Implement unfollow logic here
    }

    public boolean isFollowing(Long followerId, Long followingId) {
        // Implement check logic here
        return false;
    }

    public List<Follow> getFollowers(Long userId) {
        // Implement get followers logic here
        return null;
    }

    public List<Follow> getFollowings(Long userId) {
        // Implement get followings logic here
        return null;
    }

    public long countFollowers(Long userId) {
        // Implement count followers logic here
        return 0L;
    }

    public long countFollowings(Long userId) {
        // Implement count followings logic here
        return 0L;
    }
}
