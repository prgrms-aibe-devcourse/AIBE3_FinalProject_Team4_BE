package com.back.domain.shorlog.shorlogimage.repository;

import com.back.domain.shorlog.shorlogimage.entity.ShorlogImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShorlogImageRepository extends JpaRepository<ShorlogImage, Long> {

    Optional<ShorlogImage> findBySavedFilename(String savedFilename);

    @Query("SELECT si FROM ShorlogImage si WHERE si.referenceCount = 0 AND si.createDate < :expiryDate")
    List<ShorlogImage> findUnusedImages(@Param("expiryDate") LocalDateTime expiryDate);
}

