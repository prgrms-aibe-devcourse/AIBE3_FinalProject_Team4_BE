package com.back.domain.shorlog.shorlogimage.entity;

import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "shorlog_image")
public class ShorlogImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "saved_filename", nullable = false, unique = true)
    private String savedFilename;

    @Lob
    @Column(name = "image_data", nullable = false, columnDefinition = "LONGBLOB")
    private byte[] imageData;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "reference_count", nullable = false)
    @Builder.Default
    private Integer referenceCount = 0;

    public void incrementReference() {
        this.referenceCount++;
    }

    public void decrementReference() {
        if (this.referenceCount > 0) {
            this.referenceCount--;
        }
    }
}

