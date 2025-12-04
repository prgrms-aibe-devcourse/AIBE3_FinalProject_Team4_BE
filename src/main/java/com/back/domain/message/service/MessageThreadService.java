package com.back.domain.message.service;

import com.back.domain.message.dto.CreateMessageThreadResponseDto;
import com.back.domain.message.dto.MessageDto;
import com.back.domain.message.dto.MessageThreadListResponseDto;
import com.back.domain.message.dto.MessageThreadResponseDto;
import com.back.domain.message.entity.Message;
import com.back.domain.message.entity.MessageThread;
import com.back.domain.message.exception.MessageErrorCase;
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

    @Transactional(readOnly = true)
    public List<MessageThreadListResponseDto> getAllThreads(Long myUserId) {
        List<MessageThread> messageThreads = messageThreadRepository.findByUserId1OrUserId2(myUserId, myUserId);

        List<Long> otherUserIds = messageThreads.stream()
                .map(t -> myUserId.equals(t.getUserId1()) ? t.getUserId2() : t.getUserId1())
                .distinct()
                .toList();

        Map<Long, User> userMap = userRepository.findAllById(otherUserIds).stream()
                .collect(Collectors.toMap(User::getId, u -> u));

        return messageThreads.stream()
                .map(thread -> {
                    Long otherUserId = myUserId.equals(thread.getUserId1())
                            ? thread.getUserId2()
                            : thread.getUserId1();

                    User otherUser = userMap.get(otherUserId);
                    if (otherUser == null) {
                        // MVP: 데이터 이상 케이스는 스킵하거나 기본 User로 처리(택1)
                        return null;
                    }

                    Optional<Message> lastMessage =
                            messageRepository.findTop1ByMessageThreadIdOrderByIdDesc(thread.getId());

                    String lastMessageContent = lastMessage.map(Message::getContent).orElse(null);
                    LocalDateTime lastMessageCreatedAt = lastMessage.map(Message::getCreatedAt).orElse(null);

                    return new MessageThreadListResponseDto(
                            thread,
                            otherUser,
                            lastMessageContent,
                            lastMessageCreatedAt
                    );
                })
                .filter(Objects::nonNull)
                .sorted(
                        Comparator.comparing(
                                        MessageThreadListResponseDto::lastMessageCreatedAt,
                                        Comparator.nullsLast(Comparator.naturalOrder())
                                )
                                .reversed()
                )
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
        List<Message> messages = messageRepository.findByMessageThreadIdOrderByIdAsc(threadId);
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

       MessageThread messageThread = messageThreadRepository.findByUserId1AndUserId2(lowerId, higherId)
               .orElseGet(() -> {
                   MessageThread newThread = new MessageThread(lowerId, higherId);
                   return messageThreadRepository.save(newThread);
               });

        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        return new CreateMessageThreadResponseDto(messageThread, otherUser);
    }
}
