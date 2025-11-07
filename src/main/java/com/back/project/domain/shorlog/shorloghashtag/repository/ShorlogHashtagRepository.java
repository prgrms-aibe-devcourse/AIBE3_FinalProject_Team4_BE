package com.back.project.domain.shorlog.shorloghashtag.repository;

import com.back.project.domain.shorlog.shorloghashtag.entity.ShorlogHashtag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShorlogHashtagRepository extends JpaRepository<ShorlogHashtag, Long> {

    @Query("SELECT h.name FROM ShorlogHashtag sh JOIN sh.hashtag h WHERE sh.shorlog.id = :shorlogId")
    List<String> findHashtagNamesByShorlogId(@Param("shorlogId") Long shorlogId);

    @Modifying
    @Query("DELETE FROM ShorlogHashtag sh WHERE sh.shorlog.id = :shorlogId")
    void deleteByShorlogId(@Param("shorlogId") Long shorlogId);
}
