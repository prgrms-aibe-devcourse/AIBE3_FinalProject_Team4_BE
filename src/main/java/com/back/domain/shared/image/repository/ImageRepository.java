package com.back.domain.shared.image.repository;

import com.back.domain.shared.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    Optional<Image> findBySavedFilename(String savedFilename);

    @Query("SELECT i FROM Image i WHERE i.referenceCount = 0 AND i.createdAt < :expiryDate")
    List<Image> findUnusedImages(@Param("expiryDate") LocalDateTime expiryDate);

    @Modifying
    @Query("UPDATE Image i SET i.referenceCount = i.referenceCount + 1 WHERE i.id = :imageId")
    int incrementReferenceCount(@Param("imageId") Long imageId);

    @Modifying
    @Query("UPDATE Image i SET i.referenceCount = i.referenceCount - 1 WHERE i.id = :imageId AND i.referenceCount > 0")
    int decrementReferenceCount(@Param("imageId") Long imageId);
}

