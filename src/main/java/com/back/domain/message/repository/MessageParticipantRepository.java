package com.back.domain.message.repository;

import com.back.domain.message.entity.MessageParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MessageParticipantRepository extends JpaRepository<MessageParticipant, Long> {
    Optional<MessageParticipant> findByMessageThreadIdAndUserId(Long threadId, Long userId);

    boolean existsByMessageThreadIdAndUserId(Long threadId, Long userId);
}

