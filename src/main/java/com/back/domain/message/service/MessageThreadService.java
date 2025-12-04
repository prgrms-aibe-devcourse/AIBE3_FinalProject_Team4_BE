package com.back.domain.message.service;

import com.back.domain.message.dto.*;
import com.back.domain.message.entity.Message;
import com.back.domain.message.entity.MessageParticipant;
import com.back.domain.message.entity.MessageThread;
import com.back.domain.message.exception.MessageErrorCase;
import com.back.domain.message.repository.MessageParticipantRepository;
import com.back.domain.message.repository.MessageRepository;
import com.back.domain.message.repository.MessageThreadRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.exception.UserErrorCase;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageThreadService {
    private final UserRepository userRepository;
    private final MessageThreadRepository messageThreadRepository;
    private final MessageRepository messageRepository;
    private final MessageParticipantRepository messageParticipantRepository;

    @Transactional(readOnly = true)
    public List<MessageThreadListResponseDto> getAllThreads(Long myUserId) {
        List<MessageThread> threads = messageThreadRepository.findByUserId1OrUserId2(myUserId, myUserId);

        // 상대 유저들 미리 조회
        List<Long> otherUserIds = threads.stream()
                .map(t -> myUserId.equals(t.getUserId1()) ? t.getUserId2() : t.getUserId1())
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(otherUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return threads.stream()
                .map(thread -> {
                    MessageParticipant mePart = messageParticipantRepository
                            .findByMessageThreadIdAndUserId(thread.getId(), myUserId)
                            .orElse(null);

                    if (mePart == null) return null;
                    if ("LEFT".equals(mePart.getStatus())) return null;

                    long fromId = mePart.getVisibleFromMessageId() == null ? 0L : mePart.getVisibleFromMessageId();

                    Long otherId = myUserId.equals(thread.getUserId1()) ? thread.getUserId2() : thread.getUserId1();
                    User otherUser = userMap.get(otherId);
                    if (otherUser == null) return null;

                    // ✅ 내 visibleFrom 이후의 마지막 메시지
                    Optional<Message> lastMessageOpt =
                            messageRepository.findTop1ByMessageThreadIdAndIdGreaterThanEqualOrderByIdDesc(thread.getId(), fromId);

                    String lastContent = lastMessageOpt.map(Message::getContent).orElse(null);
                    LocalDateTime lastAt = lastMessageOpt.map(Message::getCreatedAt).orElse(null);

                    long lastReadId = Optional.ofNullable(mePart.getLastReadMessageId()).orElse(0L);

                    // ✅ unread도 "내가 볼 수 있는 범위(fromId)" 기준으로 계산해야 더 정확
                    long base = Math.max(lastReadId, fromId - 1); // fromId가 0이면 -1 되지만 count에서 안전하게 0으로 처리 가능
                    long unread = messageRepository.countByMessageThreadIdAndIdGreaterThan(thread.getId(), Math.max(base, 0L));

                    return new MessageThreadListResponseDto(thread, otherUser, lastContent, lastAt, unread);
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(
                        MessageThreadListResponseDto::lastMessageCreatedAt,
                        Comparator.nullsLast(LocalDateTime::compareTo)
                ).reversed())
                .toList();

    }


    @Transactional(readOnly = true)
    public MessageThreadResponseDto getThread(Long loginId, Long threadId) {
        MessageThread thread = messageThreadRepository.findById(threadId)
                .orElseThrow(() -> new ServiceException(MessageErrorCase.MESSAGE_THREAD_NOT_FOUND));

        // 인증 사용자가 스레드의 참여자인지 확인
        if(!(loginId.equals(thread.getUserId1()) || loginId.equals(thread.getUserId2()))) {
            throw new ServiceException(MessageErrorCase.PERMISSION_DENIED);
        }

        Long otherUserId = loginId.equals(thread.getUserId1()) ? thread.getUserId2() : thread.getUserId1();
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        MessageParticipant mePart = messageParticipantRepository
                .findByMessageThreadIdAndUserId(threadId, loginId)
                .orElseThrow(() -> new ServiceException(MessageErrorCase.PERMISSION_DENIED));

        long fromId = mePart.getVisibleFromMessageId() == null ? 0L : mePart.getVisibleFromMessageId();

        List<Message> messages = messageRepository
                .findByMessageThreadIdAndIdGreaterThanEqualOrderByIdAsc(threadId, fromId);

        return new MessageThreadResponseDto(
                thread.getId(),
                otherUser,
                messages.stream().map(MessageDto::new).toList()
        );
    }

    @Transactional
    public CreateMessageThreadResponseDto createThread(Long myUserId, Long otherUserId) {
        if (myUserId.equals(otherUserId)) {
            throw new ServiceException(MessageErrorCase.CANNOT_CREATE_THREAD_WITH_SELF);
        }

        Long lowerId = Math.min(myUserId, otherUserId);
        Long higherId = Math.max(myUserId, otherUserId);

        // 1) thread 먼저 찾거나 생성
        MessageThread thread = messageThreadRepository.findByUserId1AndUserId2(lowerId, higherId)
                .orElseGet(() -> messageThreadRepository.save(new MessageThread(lowerId, higherId)));

        // 2) participant 2명 보장 (이미 있으면 만들지 않음)
        ensureParticipant(thread.getId(), lowerId);
        ensureParticipant(thread.getId(), higherId);

        restoreFromCreateThread(thread.getId(), myUserId);

        // 응답용 otherUser
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        return new CreateMessageThreadResponseDto(thread, otherUser);
    }

    private void restoreFromCreateThread(Long threadId, Long requesterId) {
        MessageParticipant mp = messageParticipantRepository
                .findByMessageThreadIdAndUserId(threadId, requesterId)
                .orElseThrow(() -> new ServiceException(MessageErrorCase.PERMISSION_DENIED));

        if (!"LEFT".equals(mp.getStatus())) return;

        long lastId = messageRepository.findTop1ByMessageThreadIdOrderByIdDesc(threadId)
                .map(Message::getId)
                .orElse(0L);

        mp.setStatus("ACTIVE");

        // ✅ "처음 시작처럼": 기존 메시지는 안 보이게
        mp.setVisibleFromMessageId(lastId + 1);

        // ✅ 목록 unread 꼬임 방지(강추)
        mp.setLastReadMessageId(lastId);
    }

    private void ensureParticipant(Long threadId, Long userId) {
        boolean exists = messageParticipantRepository
                .findByMessageThreadIdAndUserId(threadId, userId)
                .isPresent();

        if (exists) return;

        MessageThread threadRef = messageThreadRepository.getReferenceById(threadId);
        User userRef = userRepository.getReferenceById(userId);

        messageParticipantRepository.save(MessageParticipant.create(threadRef, userRef));
    }

    @Transactional
    public ReadMessageThreadResponseDto markAsRead(Long myId, Long threadId, Long lastMessageId) {
        // 1) thread 존재 확인
        MessageThread thread = messageThreadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Thread not found"));

        // 2) participant 조회(없으면 접근 불가 or 생성 정책 택1)
        MessageParticipant mp = messageParticipantRepository
                .findByMessageThreadIdAndUserId(threadId, myId)
                .orElseThrow(() -> new IllegalArgumentException("Not a participant"));

        // 3) lastMessageId 없으면 최신 메시지 id로
        if (lastMessageId == null || lastMessageId <= 0) {
            lastMessageId = messageRepository.findTop1ByMessageThreadIdOrderByIdDesc(threadId)
                    .map(Message::getId)
                    .orElse(0L);
        }

        // 4) 되돌리기 방지
        Long cur = mp.getLastReadMessageId() == null ? 0L : mp.getLastReadMessageId();
        mp.setLastReadMessageId(Math.max(cur, lastMessageId));
        messageParticipantRepository.save(mp);

        return new ReadMessageThreadResponseDto(threadId, mp.getLastReadMessageId());
    }

    @Transactional
    public void leaveThread(Long meId, Long threadId) {
        MessageThread thread = messageThreadRepository.findById(threadId)
                .orElseThrow(() -> new ServiceException(MessageErrorCase.MESSAGE_THREAD_NOT_FOUND));

        if (!(meId.equals(thread.getUserId1()) || meId.equals(thread.getUserId2()))) {
            throw new ServiceException(MessageErrorCase.PERMISSION_DENIED);
        }

        long lastId = messageRepository.findTop1ByMessageThreadIdOrderByIdDesc(threadId)
                .map(Message::getId)
                .orElse(0L);

        MessageParticipant mp = messageParticipantRepository
                .findByMessageThreadIdAndUserId(threadId, meId)
                .orElseThrow(() -> new ServiceException(MessageErrorCase.PERMISSION_DENIED));

        mp.setStatus("LEFT");
        mp.setVisibleFromMessageId(lastId + 1); // ✅ 이후만 보이게 (새 대화처럼)
        mp.setLastReadMessageId(lastId);        // ✅ unread 0 (선택이지만 강추)
    }
}
