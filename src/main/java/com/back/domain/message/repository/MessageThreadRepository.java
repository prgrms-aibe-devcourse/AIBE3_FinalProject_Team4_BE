package com.back.domain.message.repository;

import com.back.domain.message.entity.MessageThread;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageThreadRepository extends JpaRepository<MessageThread, Long> {
    Optional<MessageThread> findByUserId1AndUserId2(Long lowerId, Long higherId);

    List<MessageThread> findByUserId1OrUserId2(Long myUserId, Long myUserId1);
}
