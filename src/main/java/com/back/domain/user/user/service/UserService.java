package com.back.domain.user.user.service;

import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.bookmark.repository.BlogBookmarkRepository;
import com.back.domain.blog.like.repository.BlogLikeRepository;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogbookmark.repository.ShorlogBookmarkRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import com.back.domain.user.user.dto.*;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.exception.UserErrorCase;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)

    public List<UserListResponseDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(user -> {
                    long followersCount = 0L;   // todo : 팔로잉 기능 추가 시 구현
                    return new UserListResponseDto(user, followersCount);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ProfileResponseDto getUserById(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        long followersCount = 0L;   // todo : 팔로잉 기능 추가 시 구현
        long followingCount = 0L;   // todo : 팔로잉 기능 추가 시 구현
        long bloglikesCount = blogLikeRepository.countAllByUser(user);
        long shorlogLikesCount = shorlogLikeRepository.countAllByUser(user);
        long likesCount = bloglikesCount + shorlogLikesCount;
        int shorlogsCount = shorlogRepository.countAllByUser(user);
        int blogsCount = blogRepository.countAllByUser(user);

        return new ProfileResponseDto(user, followersCount, followingCount, likesCount, shorlogsCount, blogsCount);
    }

    @Transactional(readOnly = true)
    public MyProfileResponseDto getMyUser(Long userId) {
        User user = userRepository.findById(userId).
                orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        long followersCount = 0L;   // todo : 팔로잉 기능 추가 시 구현
        long followingCount = 0L;   // todo : 팔로잉 기능 추가 시 구현
        long bloglikesCount = blogLikeRepository.countAllByUser(user);
        long shorlogLikesCount = shorlogLikeRepository.countAllByUser(user);
        long likesCount = bloglikesCount + shorlogLikesCount;
        int shorlogsCount = shorlogRepository.countAllByUser(user);
        int blogsCount = blogRepository.countAllByUser(user);
        int shorlogBookmarksCount = shorlogBookmarkRepository.countAllByUser(user);
        int blogBookmarksCount = blogBookmarkRepository.countAllByUser(user);

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
}
