package com.back.domain.ai.model.repository;

import com.back.domain.ai.model.entity.ModelUsage;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ModelUsageRepository extends JpaRepository<ModelUsage, Long> {

    Optional<ModelUsage> findByUserIdAndModelId(Long userId, Long modelId);

    // 동시성 안전을 위한 pessimistic lock
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT mu FROM ModelUsage mu WHERE mu.user.id = :userId AND mu.model.id = :modelId")
    Optional<ModelUsage> findByUserIdAndModelIdForUpdate(Long userId, Long modelId);

}