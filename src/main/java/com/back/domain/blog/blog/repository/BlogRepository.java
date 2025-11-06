package com.back.domain.blog.blog.repository;

import com.back.domain.blog.blog.entity.Blog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogRepository extends JpaRepository<Blog, Long> {
}
