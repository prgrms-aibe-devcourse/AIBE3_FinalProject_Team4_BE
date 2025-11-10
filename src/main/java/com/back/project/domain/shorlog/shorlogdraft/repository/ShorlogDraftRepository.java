package com.back.project.domain.shorlog.shorlogdraft.repository;

import com.back.project.domain.shorlog.shorlogdraft.entity.ShorlogDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShorlogDraftRepository extends JpaRepository<ShorlogDraft, Long> {

    List<ShorlogDraft> findByUserIdOrderByCreateDateDesc(Long userId);

    int countByUserId(Long userId);

    @Modifying
    @Query("DELETE FROM ShorlogDraft sd WHERE sd.createDate < :expiryDate")
    void deleteExpiredDrafts(@Param("expiryDate") LocalDateTime expiryDate);
}

