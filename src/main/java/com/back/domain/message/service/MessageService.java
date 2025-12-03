package com.back.domain.message.service;

import com.back.domain.message.entity.Message;
import com.back.domain.message.repository.MessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;

    public Message createMessage(Message message) {
        return messageRepository.save(message);
    }
}
