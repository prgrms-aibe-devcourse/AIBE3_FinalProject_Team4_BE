package com.back.domain.user.user.service;

import com.back.domain.user.user.dto.UpdateProfileRequestDto;
import com.back.domain.user.user.dto.UserDto;
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

    @Transactional(readOnly = true)
    public List<UserDto> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream()
                .map(UserDto::new)
                .toList();
    }

    @Transactional(readOnly = true)
    public UserDto getUserById(Long userId) {
        User user =  userRepository.findById(userId).
                orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));
        return new UserDto(user);
    }

    @Transactional
    public UserDto updateProfile(Long userId, UpdateProfileRequestDto dto) {
        if(!isAvailableNickname(dto.nickname())) {
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
