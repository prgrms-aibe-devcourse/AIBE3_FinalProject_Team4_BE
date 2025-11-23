package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blogFile.dto.BlogFileDto;
import com.back.domain.blog.blogFile.entity.BlogFile;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

public record BlogWriteDto(
        Long id,
        String title,
        String content,
        Long userId,
        String thumbnailUrl,
        List<BlogFileDto> images,
        List<String> hashtagNames,
        String status,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {

    public BlogWriteDto(Blog blog) {
        this(
                blog.getId(),
                blog.getTitle(),
                blog.getContent(),
                blog.getUser().getId(),
                blog.getThumbnailUrl(),
                blog.getBlogFiles().stream()
                        .sorted(Comparator.comparing(BlogFile::getSortOrder)).map(BlogFileDto::new).toList(),
                blog.getBlogHashtags().stream()
                        .map(blogHashtag -> blogHashtag.getHashtag().getName())
                        .toList(),
                blog.getStatus().name(),
                blog.getCreatedAt(),
                blog.getModifiedAt()
        );
    }
}
