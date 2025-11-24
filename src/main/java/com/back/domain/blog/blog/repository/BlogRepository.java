package com.back.domain.blog.blog.repository;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.entity.BlogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BlogRepository extends JpaRepository<Blog, Long>, BlogRepositoryCustom {

    @Query("""
            SELECT b FROM Blog b
            WHERE b.status = :blogStatus AND b.user.id = :id
            """)
    List<Blog> findByStatusAndUserId(BlogStatus blogStatus, Long id);

    @Query("select coalesce(b.likeCount, 0) from Blog b where b.id = :blogId")
    long getLikeCountById(Long blogId);

    @Query("select coalesce(b.bookmarkCount, 0) from Blog b where b.id = :blogId")
    Optional<Long> getBookmarkCountById(Long blogId);

    int countAllByUserId(Long userId);

    List<Blog> findByStatusAndModifiedAtBefore(BlogStatus status, LocalDateTime cutoff);

    @Query("""
            select b from Blog b
            where b.user.id = :userId
            and b.status = 'PUBLISHED'
            order by b.modifiedAt desc
            """)
    List<Blog> findRecentBlogsByUserId(Long userId, PageRequest pageRequest);

    //  LAZY 방지용 fetch join
    @Query("""
                select b
                from Blog b
                left join fetch b.user
                left join fetch b.blogFiles bf
                left join fetch bf.image
                where b.id = :id
            """)
    Optional<Blog> findDetailWithFiles(@Param("id") Long id);

    //  인덱서용 fetch join 메서드
    @Query("""
            select distinct b
            from Blog b
                 left join fetch b.user u
                 left join fetch b.blogHashtags bh
                 left join fetch bh.hashtag h
            where b.id = :id
            """)
    Optional<Blog> findForIndexingById(@Param("id") Long id);

    @Query("""
                select h.name
                from BlogHashtag bh
                join bh.hashtag h
                where bh.blog.id = :blogId
            """)
    List<String> findHashtagNamesByBlogId(@Param("blogId") Long blogId);


    Page<Blog> findAllByUserIdAndStatus(Long userId, BlogStatus blogStatus, Pageable pageable);

    List<Blog> findRecentBlogsByUserId(Long userId);

    @Query("""
            SELECT b.id, b.createdAt
            FROM Blog b
            WHERE b.user.id = :userId
            ORDER BY b.id DESC
            """)
    Page<Object[]> findUserBlogActivities(@Param("userId") Long userId, Pageable pageable);
}
