package com.back.domain.blog.blog.repository;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long> {
    @Query("select distinct b from Blog b " +
            "left join fetch b.blogHashtags bh " +
            "left join fetch bh.hashtag h")
    List<Blog> findAllWithHashtags();

    @Query("""
                SELECT b FROM Blog b
                JOIN FETCH b.blogHashtags bh
                JOIN FETCH bh.hashtag
                WHERE b.id = :id
            """)
    Optional<Blog> findByIdWithHashtags(@Param("id") Long id);

    List<Blog> findByStatusAndUserId(BlogStatus blogStatus, Long id);

    @Modifying
    @Query("""
            update Blog b set b.bookmarkCount = b.bookmarkCount + 1 where b.id = :blogId
            """)
    void increaseBookmark(Long blogId);

    @Modifying
    @Query("""
            update Blog b set b.bookmarkCount = 
              case when b.bookmarkCount > 0 then b.bookmarkCount - 1 else 0 end
            where b.id = :blogId
            """)
    void decreaseBookmark(Long postId);

    @Query("SELECT b FROM Blog b WHERE b.user = :author AND b.status = :status ORDER BY b.createdAt DESC")
    List<Blog> findAllByAuthorAndStatus(Long id, BlogStatus blogStatus);
}
