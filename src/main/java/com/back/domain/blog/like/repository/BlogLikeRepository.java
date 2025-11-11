package com.back.domain.blog.like.repository;

import com.back.domain.blog.like.entity.BlogLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogLikeRepository extends JpaRepository<BlogLike, Long> {
}
