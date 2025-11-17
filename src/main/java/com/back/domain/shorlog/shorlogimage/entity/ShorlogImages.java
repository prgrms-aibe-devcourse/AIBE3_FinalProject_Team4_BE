package com.back.domain.shorlog.shorlogimage.entity;

import com.back.domain.shared.image.entity.Image;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
    name = "shorlog_images",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_shorlog_image", columnNames = {"shorlog_id", "image_id"})
    },
    indexes = {
        @Index(name = "idx_shorlog_id", columnList = "shorlog_id")
    }
)
public class ShorlogImages extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorlog_id", nullable = false)
    private Shorlog shorlog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    public static ShorlogImages create(Shorlog shorlog, Image image, Integer sortOrder) {
        ShorlogImages shorlogImages = new ShorlogImages();
        shorlogImages.shorlog = shorlog;
        shorlogImages.image = image;
        shorlogImages.sortOrder = sortOrder;
        return shorlogImages;
    }
}
