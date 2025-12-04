package com.back.domain.message.entity;

import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(
        name = "message_participant",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_participant_thread_user", columnNames = {"message_thread_id", "user_id"})
        }
)
public class MessageParticipant extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    private MessageThread messageThread;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user;

    private Long lastReadMessageId;

    private LocalDateTime lastReadAt;

    private String status;  // ACTIVE, LEFT

    Long visibleFromMessageId = 0L;

    public static MessageParticipant create(MessageThread thread, User user) {
        MessageParticipant mp = new MessageParticipant();
        mp.messageThread = thread;
        mp.user = user;
        mp.lastReadMessageId = 0L;
        mp.lastReadAt = LocalDateTime.now();
        return mp;
    }
}
