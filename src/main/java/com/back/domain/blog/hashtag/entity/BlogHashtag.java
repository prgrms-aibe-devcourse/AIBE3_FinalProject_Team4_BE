package com.back.domain.blog.hashtag.entity;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.hashtag.hashtag.entity.Hashtag;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Setter
@NoArgsConstructor
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

    public BlogHashtag(Long id, Blog blog) {
        this.id = id;
        this.blog = blog;
    }

    public static BlogHashtag createBlogHashtag(Blog blog, Hashtag hashtag) {
        BlogHashtag blogHashtag = new BlogHashtag();
        blogHashtag.setBlog(blog);
        blogHashtag.setHashtag(hashtag);
        return blogHashtag;
    }
}
