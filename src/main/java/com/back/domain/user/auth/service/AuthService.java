package com.back.domain.user.auth.service;

import com.back.domain.user.mail.service.VerificationTokenService;
import com.back.domain.user.refreshToken.service.RefreshTokenService;
import com.back.domain.user.auth.dto.OAuth2CompleteJoinRequestDto;
import com.back.domain.user.auth.dto.PasswordResetRequestDto;
import com.back.domain.user.auth.dto.UserJoinRequestDto;
import com.back.domain.user.auth.dto.UserLoginRequestDto;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.config.security.jwt.JwtTokenProvider;
import com.back.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final VerificationTokenService verificationTokenService;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public User join(UserJoinRequestDto dto) {
        boolean isValid = verificationTokenService.isValidToken(dto.email(), dto.verificationToken());
        if (!isValid) {
            throw new AuthException("400-1", "이메일 인증을 먼저 완료해주세요.");
        }

        userRepository.findByUsername(dto.username()).ifPresent(_user -> {
            throw new AuthException("400-2", "이미 가입된 아이디입니다.");
        });
        userRepository.findByNickname(dto.nickname()).ifPresent(_user -> {
            throw new AuthException("400-3", "이미 가입된 닉네임입니다.");
        });

        verificationTokenService.deleteToken(dto.email());

        String password = passwordEncoder.encode(dto.password());
        User user = new User(dto.email(), dto.username(), password, dto.nickname(), dto.dateOfBirth(), dto.gender());
        return userRepository.save(user);
    }

    @Transactional
    public User joinOrLoginOAuth2User(String username, String profileImgUrl) {
        User user = userRepository.findByUsername(username).orElse(null);
        if(user == null) {
            user = new User(username, profileImgUrl);
            return userRepository.save(user);
        }
        user.updateProfileImgUrl(profileImgUrl);
        return user;
    }

    @Transactional
    public User toCompleteJoinOAuth2User(OAuth2CompleteJoinRequestDto dto) {
        String token = dto.temporaryToken();
        if(!jwtTokenProvider.validateToken(token)) {
            throw new AuthException("400-1", "임시토큰이 만료되었습니다. 처음부터 다시 시도해주세요.");
        }
        Long userId = jwtTokenProvider.getUserId(token);
        User user = getUserById(userId);
        user.completeOAuth2Join(dto.nickname(), dto.dateOfBirth(), dto.gender());
        return user;
    }

    @Transactional
    public User login(UserLoginRequestDto dto) {
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new AuthException("401-1", "존재하지 않는 아이디입니다."));

        checkPassword(user, dto.password());

        return user;
    }

    @Transactional
    public void logout(Long userId) {
        refreshTokenService.deleteRefreshTokenByUserId(userId);
        SecurityContextHolder.clearContext();
    }

    @Transactional
    public void passwordReset(PasswordResetRequestDto dto) {
        boolean isValid = verificationTokenService.isValidToken(dto.email(), dto.verificationToken());
        if (!isValid) {
            throw new AuthException("400-1", "이메일 인증을 먼저 완료해주세요.");
        }

        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new AuthException("401-1", "존재하지 않는 아이디입니다."));

        verificationTokenService.deleteToken(dto.email());

        String encodedPassword = passwordEncoder.encode(dto.newPassword());
        user.updatePassword(encodedPassword);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AuthException("401-1", "존재하지 않는 회원입니다."));
    }

    @Transactional(readOnly = true)
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthException("401-1", "존재하지 않는 회원입니다."));
    }

    @Transactional
    public void checkPassword(User user, String password) {
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new AuthException("401-1", "비밀번호가 일치하지 않습니다.");
        }
    }
}
