package com.back.domain.shorlog.shorlogimage.repository;

import com.back.domain.shared.image.entity.Image;
import com.back.domain.shorlog.shorlogimage.entity.ShorlogImages;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ShorlogImagesRepository extends JpaRepository<ShorlogImages, Long> {

    @Query("SELECT si.image FROM ShorlogImages si WHERE si.shorlog.id = :shorlogId ORDER BY si.sortOrder ASC")
    List<Image> findAllImagesByShorlogIdOrderBySort(@Param("shorlogId") Long shorlogId);

    @Modifying
    @Query("DELETE FROM ShorlogImages si WHERE si.shorlog.id = :shorlogId")
    void deleteByShorlogId(@Param("shorlogId") Long shorlogId);

    // 쇼로그들의 썸네일 이미지 URL 조회
    @Query(value = """
        SELECT si.shorlog_id AS shorlogId, img.s3_url AS url
        FROM shorlog_images si
        JOIN images img ON img.id = si.image_id
        JOIN (
            SELECT shorlog_id, MIN(sort_order) AS minSort
            FROM shorlog_images
            WHERE shorlog_id IN (:shorlogIds)
            GROUP BY shorlog_id
        ) m ON m.shorlog_id = si.shorlog_id AND si.sort_order = m.minSort
        WHERE si.shorlog_id IN (:shorlogIds)
        """, nativeQuery = true)
    List<Object[]> findFirstImageUrlByShorlogIds(@Param("shorlogIds") List<Long> shorlogIds);
}

