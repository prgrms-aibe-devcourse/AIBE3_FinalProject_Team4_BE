package com.back.domain.blog.blogFile.entity;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.shared.image.entity.Image;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Objects;

@Table(name = "blog_files")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlogFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "blogFile_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Column(name = "sort_order")
    private Integer sortOrder;

    private BlogFile(Blog blog, Image image, Integer sortOrder) {
        this.blog = Objects.requireNonNull(blog);
        this.image = Objects.requireNonNull(image);
        this.sortOrder = sortOrder;
    }

    public static BlogFile create(Blog blog, Image image, Integer sortOrder) {
        return new BlogFile(blog, image, sortOrder);
    }
}