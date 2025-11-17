package com.back.domain.shared.image.entity;

import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "images",
    indexes = {
        @Index(name = "idx_user_id", columnList = "user_id")
    }
)
public class Image extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ImageType type;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "original_filename", nullable = false, length = 255)
    private String originalFilename;

    @Column(name = "saved_filename", nullable = false, unique = true, length = 255)
    private String savedFilename;

    @Column(name = "s3_url", nullable = false, length = 512)
    private String s3Url;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "reference_count", nullable = false)
    private Integer referenceCount = 0;

    private Image(User user, ImageType type, String originalFilename, String savedFilename,
                  String s3Url, long fileSize, String contentType, int referenceCount) {
        this.user = user;
        this.type = type;
        this.originalFilename = originalFilename;
        this.savedFilename = savedFilename;
        this.s3Url = s3Url;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.referenceCount = referenceCount;
    }

    public static Image create(User user, ImageType type, String originalFilename,
                               String savedFilename, String s3Url, long fileSize, String contentType) {
        return new Image(user, type, originalFilename, savedFilename, s3Url, fileSize, contentType, 0);
    }
}




