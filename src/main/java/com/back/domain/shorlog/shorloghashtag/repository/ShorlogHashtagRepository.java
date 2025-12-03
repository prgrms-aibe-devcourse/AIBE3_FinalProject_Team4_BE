package com.back.domain.shorlog.shorloghashtag.repository;

import com.back.domain.shorlog.shorloghashtag.entity.ShorlogHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ShorlogHashtagRepository extends JpaRepository<ShorlogHashtag, Long> {

    @Query("SELECT h.name FROM ShorlogHashtag sh JOIN sh.hashtag h WHERE sh.shorlog.id = :shorlogId")
    List<String> findHashtagNamesByShorlogId(@Param("shorlogId") Long shorlogId);

    @Query("SELECT CASE WHEN COUNT(sh) > 0 THEN true ELSE false END FROM ShorlogHashtag sh WHERE sh.shorlog.id = :shorlogId AND sh.hashtag.id = :hashtagId")
    boolean existsByShorlogIdAndHashtagId(@Param("shorlogId") Long shorlogId, @Param("hashtagId") Long hashtagId);

    @Modifying(clearAutomatically = true)
    @Query("DELETE FROM ShorlogHashtag sh WHERE sh.shorlog.id = :shorlogId")
    void deleteByShorlogId(@Param("shorlogId") Long shorlogId);

    @Query("""
        SELECT sh.hashtag.name, COUNT(sh)
        FROM ShorlogHashtag sh
        WHERE sh.shorlog.createdAt >= :from
        GROUP BY sh.hashtag.name
        """)
    List<Object[]> countHashtagUsageSince(LocalDateTime from);
}
