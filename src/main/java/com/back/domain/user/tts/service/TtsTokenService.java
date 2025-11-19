package com.back.domain.user.tts.service;

import com.back.domain.shorlog.shorlogtts.dto.TtsTokenResponse;
import com.back.domain.shorlog.shorlogtts.dto.TtsUseResponse;
import com.back.domain.shorlog.shorlogtts.exception.TtsErrorCase;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TtsTokenService {

    private final UserRepository userRepository;

    @Transactional
    public TtsTokenResponse getToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(TtsErrorCase.USER_NOT_FOUND));

        // 기존 사용자의 경우 null일 수 있으므로 초기화
        Integer ttsToken = user.getTtsToken();
        if (ttsToken == null) {
            user.resetTtsToken();
            ttsToken = 100;
        }

        // 다음 리셋 날짜 계산 (매월 1일 00:00)
        LocalDateTime lastReset = user.getTtsLastReset() != null ?
                user.getTtsLastReset() : LocalDateTime.now();
        LocalDateTime nextReset = calculateNextResetDate(lastReset);

        return TtsTokenResponse.of(ttsToken, nextReset);
    }

    @Transactional
    public TtsUseResponse useToken(Long userId, int amount) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(TtsErrorCase.USER_NOT_FOUND));

        user.useTtsToken(amount);

        log.info("TTS 토큰 차감 - 사용자 ID: {}, 차감량: {}, 남은 토큰: {}",
                userId, amount, user.getTtsToken());

        return TtsUseResponse.of(true, user.getTtsToken());
    }

    // 월 1일 00:00에 모든 사용자 토큰 리셋
    @Scheduled(cron = "0 0 0 1 * *")
    @Transactional
    public void resetAllTokens() {
        List<User> users = userRepository.findAll();

        for (User user : users) {
            user.resetTtsToken();
        }

        log.info("TTS 토큰 리셋 완료 - 총 {} 명", users.size());
    }

    private LocalDateTime calculateNextResetDate(LocalDateTime lastReset) {
        LocalDateTime now = LocalDateTime.now();

        if (now.getDayOfMonth() == 1 && now.isAfter(lastReset)) {
            return now.plusMonths(1).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        } else {
            return now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        }
    }
}

