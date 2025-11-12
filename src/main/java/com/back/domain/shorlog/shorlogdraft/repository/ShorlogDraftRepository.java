package com.back.domain.shorlog.shorlogdraft.repository;

import com.back.domain.shorlog.shorlogdraft.entity.ShorlogDraft;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShorlogDraftRepository extends JpaRepository<ShorlogDraft, Long> {

    List<ShorlogDraft> findByUserIdOrderByCreatedAtDesc(Long userId);

    int countByUserId(Long userId);

    @Modifying(clearAutomatically = true) // Bulk DELETE 처리
    @Query("DELETE FROM ShorlogDraft sd WHERE sd.createdAt < :expiryDate")
    void deleteExpiredDrafts(@Param("expiryDate") LocalDateTime expiryDate);
}
