package com.back.domain.blog.bloghashtag.entity;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.shared.hashtag.entity.Hashtag;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "blog_hashtags")
public class BlogHashtag extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id")
    private Hashtag hashtag;

    public BlogHashtag(Blog blog, Hashtag hashtag) {
        this.blog = blog;
        this.hashtag = hashtag;
    }
}
