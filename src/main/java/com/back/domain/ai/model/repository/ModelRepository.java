package com.back.domain.ai.model.repository;

import com.back.domain.ai.model.entity.Model;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModelRepository extends JpaRepository<Model, Long> {
    Optional<Model> findByName(String name);

    @Query("""
        SELECT m, mu
        FROM Model m
        LEFT JOIN ModelUsage mu
               ON mu.model = m AND mu.user.id = :userId
    """)
    List<Object[]> findModelsWithUsage(@Param("userId") Long userId);
}
