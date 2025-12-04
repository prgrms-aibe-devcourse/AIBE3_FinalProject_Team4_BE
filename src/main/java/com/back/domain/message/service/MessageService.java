package com.back.domain.message.service;

import com.back.domain.message.dto.MessageRequestDto;
import com.back.domain.message.dto.MessageResponseDto;
import com.back.domain.message.entity.Message;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final MessageThreadRepository messageThreadRepository;
    private final UserRepository userRepository;
    private final MessageParticipantRepository messageParticipantRepository;

    @Transactional
    public MessageResponseDto save(Long meId, MessageRequestDto req) {
        if(req.content() == null || req.content().isBlank()) {
            throw new IllegalArgumentException("Message content cannot be empty");
        }

        MessageThread messageThread = messageThreadRepository.findById(req.messageThreadId())
                .orElseThrow(() -> new IllegalArgumentException("Message thread not found"));

        if(!meId.equals(messageThread.getUserId1()) && !meId.equals(messageThread.getUserId2())) {
            throw new ServiceException(MessageErrorCase.PERMISSION_DENIED);
        }

        restoreIfLeft(req.messageThreadId(), meId);

        User user = userRepository.findById(meId)
                .orElseThrow(() -> new ServiceException(UserErrorCase.USER_NOT_FOUND));

        Message message = new Message(user, req.content(), messageThread);
        messageRepository.save(message);

        return new MessageResponseDto(message);
    }

    private void restoreIfLeft(Long threadId, Long senderId) {
        MessageThread thread = messageThreadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Message thread not found"));

        Long otherId = senderId.equals(thread.getUserId1()) ? thread.getUserId2() : thread.getUserId1();

        messageParticipantRepository.findByMessageThreadIdAndUserId(threadId, senderId)
                .ifPresent(p -> { if ("LEFT".equals(p.getStatus())) p.setStatus("ACTIVE"); });

        messageParticipantRepository.findByMessageThreadIdAndUserId(threadId, otherId)
                .ifPresent(p -> { if ("LEFT".equals(p.getStatus())) p.setStatus("ACTIVE"); });
    }

}
