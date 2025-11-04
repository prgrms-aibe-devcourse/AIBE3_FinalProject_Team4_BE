package com.back.domain.user.user.service;

import com.back.domain.user.user.dto.UserJoinRequestDto;
import com.back.domain.user.user.dto.UserLoginRequestDto;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    @Transactional
    public User join(UserJoinRequestDto dto) {
        userRepository.findByUsername(dto.username()).ifPresent(_user -> {
            throw new RuntimeException("이미 가입된 아이디입니다.");
        });
        userRepository.findByEmail(dto.email()).ifPresent(_user -> {
            throw new RuntimeException("이미 가입된 이메일입니다.");
        });

        User user = new User(dto.email(), dto.username(), dto.password());
        return userRepository.save(user);
    }

    public User login(UserLoginRequestDto dto) {
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 아이디입니다."));

        if (!user.getPassword().equals(dto.password())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        return user;
    }

    public User getUserByApiKey(String apiKey) {
        return userRepository.findByApiKey(apiKey)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 API Key입니다."));
    }
}
