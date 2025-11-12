package com.back.domain.blog.hashtag.repository;

import com.back.domain.blog.hashtag.entity.BlogHashtag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogHashTagRepository extends JpaRepository<BlogHashtag, Long> {
}
