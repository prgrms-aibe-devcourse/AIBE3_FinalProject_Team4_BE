package com.back.domain.blog.hashtag;

import com.back.domain.blog.blog.entity.Blog;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BlogHashtag {
    @Id
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_id", nullable = false)
    private Blog blog;
    

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "hashtag_id", nullable = false)
//    private Hashtag hashtag;

//    public static BlogHashtag createBlogHashtag(Blog blog, Hashtag hashtag) {
//        BlogHashtag blogHashtag = new BlogHashtag();
//        blogHashtag.setBlog(blog);
//        blogHashtag.setHashtag(hashtag);
//        return blogHashtag;
//    }
}
