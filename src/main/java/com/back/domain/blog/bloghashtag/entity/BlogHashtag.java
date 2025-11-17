package com.back.domain.blog.bloghashtag.entity;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.shared.hashtag.entity.Hashtag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "blog_hashtags")
public class BlogHashtag {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    
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
