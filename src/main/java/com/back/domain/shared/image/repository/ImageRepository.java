package com.back.domain.shared.image.repository;

import com.back.domain.shared.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ImageRepository extends JpaRepository<Image, Long> {
    Optional<Image> findBySavedFilename(String savedFilename);

    @Query("SELECT si FROM Image si WHERE si.referenceCount = 0 AND si.createdAt < :expiryDate")
    List<Image> findUnusedImages(@Param("expiryDate") LocalDateTime expiryDate);
}
