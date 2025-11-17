package com.back.domain.shorlog.shorlogimage.repository;

import com.back.domain.shared.image.entity.Image;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlogimage.entity.ShorlogImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShorlogImagesRepository extends JpaRepository<ShorlogImages, Long> {

    @Query("SELECT si.image FROM ShorlogImages si WHERE si.shorlog.id = :shorlogId ORDER BY si.sortOrder ASC")
    List<Image> findAllImagesByShorlogIdOrderBySort(@Param("shorlogId") Long shorlogId);

    void deleteByShorlog(Shorlog shorlog);
}

