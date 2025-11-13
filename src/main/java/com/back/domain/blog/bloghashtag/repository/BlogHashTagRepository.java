package com.back.domain.blog.bloghashtag.repository;

import com.back.domain.blog.bloghashtag.entity.BlogHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogHashTagRepository extends JpaRepository<BlogHashtag, Long> {
}
