package com.back.domain.message.repository;

import com.back.domain.message.entity.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {
    Optional<Message> findTop1ByMessageThreadIdOrderByIdDesc(Long id);

    List<Message> findByMessageThreadIdOrderByIdAsc(Long threadId);
}
