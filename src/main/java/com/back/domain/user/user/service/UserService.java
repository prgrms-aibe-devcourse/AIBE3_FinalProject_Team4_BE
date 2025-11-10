package com.back.domain.user.user.service;

import com.back.domain.user.user.dto.UserJoinRequestDto;
import com.back.domain.user.user.dto.UserLoginRequestDto;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public User join(UserJoinRequestDto dto) {
        userRepository.findByEmail(dto.email()).ifPresent(_user -> {
            throw new AuthException("400-1", "이미 가입된 이메일입니다.");
        });
        userRepository.findByUsername(dto.username()).ifPresent(_user -> {
            throw new AuthException("400-2", "이미 가입된 아이디입니다.");
        });

        String password = passwordEncoder.encode(dto.password());
        User user = new User(dto.email(), dto.username(), password);
        return userRepository.save(user);
    }

    @Transactional
    public User login(UserLoginRequestDto dto) {
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new AuthException("401-1", "존재하지 않는 아이디입니다."));

        checkPassword(user, dto.password());

        return user;
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AuthException("401-1", "존재하지 않는 회원입니다."));
    }

    @Transactional
    public void checkPassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("401-1", "비밀번호가 일치하지 않습니다.");
        }
    }
}
