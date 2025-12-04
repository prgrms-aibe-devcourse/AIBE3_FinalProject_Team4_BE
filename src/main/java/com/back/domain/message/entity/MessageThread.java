package com.back.domain.message.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "message_thread",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_thread_user_pair", columnNames = {"user_id1", "user_id2"})
        }
)
public class MessageThread extends BaseEntity {
    private Long userId1;   // lowerId
    private Long userId2;   // higherId
    // private List<Message> messages;
}