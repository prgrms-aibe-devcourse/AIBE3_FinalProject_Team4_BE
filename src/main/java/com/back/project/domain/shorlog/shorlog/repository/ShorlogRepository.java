package com.back.project.domain.shorlog.shorlog.repository;

import com.back.project.domain.shorlog.shorlog.entity.Shorlog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ShorlogRepository extends JpaRepository<Shorlog, Long> {

    @Modifying
    @Query("UPDATE Shorlog s SET s.viewCount = s.viewCount + 1 WHERE s.id = :id")
    void incrementViewCount(@Param("id") Long id);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM Shorlog s WHERE s.id = :shorlogId AND s.user.id = :userId")
    boolean existsByIdAndUserId(@Param("shorlogId") Long shorlogId, @Param("userId") Long userId);
}
