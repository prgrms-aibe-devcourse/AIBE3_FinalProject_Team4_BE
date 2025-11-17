package com.back.domain.shared.image.entity;

import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.Objects;

@Entity
@Table(name = "images")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private ImageType type;   // THUMBNAIL, CONTENT 등

    @Column(name = "original_filename", nullable = false)
    private String originalFilename;

    @Column(name = "saved_filename", nullable = false)
    private String savedFilename;

    @Column(name = "s3_url", nullable = false, length = 512)
    private String s3Url; // 실제 접근용 URL

    @Column(name = "file_size", nullable = false)
    private long fileSize;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;   // image/png, video/mp4 ...

    @Column(name = "reference_count", nullable = false)
    private int referenceCount;

    private Image(User user, ImageType type, String originalFilename, String savedFilename, String s3Url, long fileSize, String contentType, int referenceCount) {

        this.user = Objects.requireNonNull(user);
        this.type = Objects.requireNonNull(type);
        this.originalFilename = Objects.requireNonNull(originalFilename);
        this.savedFilename = Objects.requireNonNull(savedFilename);
        this.s3Url = Objects.requireNonNull(s3Url);
        this.fileSize = fileSize;
        this.contentType = Objects.requireNonNull(contentType);
        this.referenceCount = referenceCount;
    }

    public static Image create(User user, ImageType type, String originalFilename, String savedFilename, String s3Url, long fileSize, String contentType) {

        return new Image(user, type, originalFilename, savedFilename, s3Url, fileSize, contentType, 1 // 첫 생성 시 refCount = 1
        );
    }

    public boolean isUnused() {
        return referenceCount <= 0;
    }

    public void incrementReference() {
        this.referenceCount++;
    }

    public void decrementReference() {
        if (this.referenceCount > 0) {
            this.referenceCount--;
        }
    }
}