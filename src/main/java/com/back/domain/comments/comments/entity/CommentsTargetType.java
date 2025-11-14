package com.back.domain.comments.comments.entity;

import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;

public enum CommentsTargetType {
    BLOG,
    SHORLOG;

    public boolean exists(Long targetId,
                          BlogRepository blogRepository,
                          ShorlogRepository shorlogRepository) {

        return switch (this) {
            case BLOG -> blogRepository.existsById(targetId);
            case SHORLOG -> shorlogRepository.existsById(targetId);
        };
    }
}
