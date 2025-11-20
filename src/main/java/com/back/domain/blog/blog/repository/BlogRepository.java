package com.back.domain.blog.blog.repository;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long>, BlogRepositoryCustom {
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

    @Query("""
            SELECT b FROM Blog b
            WHERE b.status = :blogStatus AND b.user.id = :id
            """)
    List<Blog> findByStatusAndUserId(BlogStatus blogStatus, Long id);

    // reaction 관련 메서드
    @Modifying(clearAutomatically = true)
    @Query("update Blog b set b.bookmarkCount = b.bookmarkCount + 1 where b.id = :blogId")
    void increaseBookmark(Long blogId);

    @Modifying(clearAutomatically = true)
    @Query("""
            update Blog b set b.bookmarkCount = 
              case when b.bookmarkCount > 0 then b.bookmarkCount - 1 else 0 end
            where b.id = :blogId
            """)
    int decreaseBookmark(Long blogId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Blog b set b.likeCount = b.likeCount + 1 where b.id = :blogId
            """)
    int increaseLikeCount(Long blogId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update Blog b set b.likeCount = 
              case when b.likeCount > 0 then b.likeCount - 1 else 0 end
            where b.id = :blogId
            """)
    int decreaseLikeCount(Long blogId);

    @Query("select coalesce(b.likeCount, 0) from Blog b where b.id = :blogId")
    long getLikeCountById(Long blogId);

    @Query("select coalesce(b.bookmarkCount, 0) from Blog b where b.id = :blogId")
    Optional<Long> getBookmarkCountById(Long blogId);

    int countAllByUserId(Long userId);
}
