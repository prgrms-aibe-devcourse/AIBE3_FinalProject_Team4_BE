package com.back.domain.blog.blogFile.repository;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blogFile.entity.BlogFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BlogFileRepository extends JpaRepository<BlogFile, Long> {
    List<BlogFile> findByBlog(Blog blog);
}
