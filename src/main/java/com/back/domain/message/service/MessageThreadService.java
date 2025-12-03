package com.back.domain.message.service;

import com.back.domain.message.entity.MessageThread;
import com.back.domain.message.repository.MessageThreadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageThreadService {
    private final MessageThreadRepository messageThreadRepository;

    @Transactional(readOnly = true)
    public List<MessageThread> getAllThreads(Long myUserId) {
        return messageThreadRepository.findByUserId1OrUserId2(myUserId, myUserId);
    }

    @Transactional(readOnly = true)
    public MessageThread getThread(Long loginId, Long threadId) {
        MessageThread thread = messageThreadRepository.findById(threadId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid thread ID"));

        if(!(loginId.equals(thread.getUserId1()) || loginId.equals(thread.getUserId2()))) {
            throw new IllegalArgumentException("Access denied to this thread");
        }

        return thread;
    }

    @Transactional
    public MessageThread createThread(Long myUserId, Long otherUserId) {
        if (myUserId.equals(otherUserId)) {
            throw new IllegalArgumentException("Cannot create thread with myself");
        }

       Long lowerId = Math.min(myUserId, otherUserId);
       Long higherId = Math.max(myUserId, otherUserId);

       return messageThreadRepository.findByUserId1AndUserId2(lowerId, higherId)
               .orElseGet(() -> {
                   MessageThread newThread = new MessageThread(lowerId, higherId);
                     return messageThreadRepository.save(newThread);
               });

    }
}
