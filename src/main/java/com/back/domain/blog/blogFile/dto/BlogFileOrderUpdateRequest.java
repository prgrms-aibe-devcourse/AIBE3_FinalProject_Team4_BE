package com.back.domain.blog.blogFile.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BlogFileOrderUpdateRequest(
        @NotNull
        List<Long> imageIds
) {
}