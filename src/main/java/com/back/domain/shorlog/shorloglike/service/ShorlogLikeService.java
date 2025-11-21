package com.back.domain.shorlog.shorloglike.service;

import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorloglike.dto.ShorlogLikeResponse;
import com.back.domain.shorlog.shorloglike.entity.ShorlogLike;
import com.back.domain.shorlog.shorloglike.event.ShorlogLikeCreatedEvent;
import com.back.domain.shorlog.shorloglike.event.ShorlogLikeDeletedEvent;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShorlogLikeService {

    private final ShorlogLikeRepository shorlogLikeRepository;
    private final ShorlogRepository shorlogRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ShorlogLikeResponse addLike(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("쇼로그를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        if (shorlogLikeRepository.existsByShorlogAndUser(shorlog, user)) {
            throw new DataIntegrityViolationException("이미 좋아요를 누른 쇼로그입니다.");
        }

        ShorlogLike shorlogLike = ShorlogLike.create(shorlog, user);

        shorlogLikeRepository.save(shorlogLike);

        long likeCount = shorlogLikeRepository.countByShorlog(shorlog);

        eventPublisher.publishEvent(new ShorlogLikeCreatedEvent(shorlogId));

        notificationService.send(
                shorlog.getUser().getId(),     // 숏로그 작성자에게 알림
                userId,                        // 좋아요 누른 사람
                NotificationType.SHORLOG_LIKE,
                shorlogId,
                user.getNickname()
        );

        return new ShorlogLikeResponse(true, likeCount);
    }

    @Transactional
    public ShorlogLikeResponse removeLike(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("쇼로그를 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        ShorlogLike shorlogLike = shorlogLikeRepository.findByShorlogAndUser(shorlog, user)
                .orElseThrow(() -> new NoSuchElementException("좋아요를 누르지 않은 쇼로그입니다."));

        shorlogLikeRepository.delete(shorlogLike);

        long likeCount = shorlogLikeRepository.countByShorlog(shorlog);

        eventPublisher.publishEvent(new ShorlogLikeDeletedEvent(shorlogId));

        return new ShorlogLikeResponse(false, likeCount);
    }

    public ShorlogLikeResponse getLikeStatus(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new NoSuchElementException("쇼로그를 찾을 수 없습니다."));

        long likeCount = shorlogLikeRepository.countByShorlog(shorlog);

        boolean isLiked = false;
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));
            isLiked = shorlogLikeRepository.existsByShorlogAndUser(shorlog, user);
        }

        return new ShorlogLikeResponse(isLiked, likeCount);
    }
}

