package com.back.domain.message.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MessageThread extends BaseEntity {
    private Long userId1;   // lowerId
    private Long userId2;   // higherId
    // private List<Message> messages;
}