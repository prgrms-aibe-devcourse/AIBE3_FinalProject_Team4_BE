package com.back.domain.blog.blogdoc.dto;

import jakarta.validation.constraints.NotBlank;

public record BlogDocWriteRequest(
        @NotBlank String title,
        @NotBlank String content
) {
}
