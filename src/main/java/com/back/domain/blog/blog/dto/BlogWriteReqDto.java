package com.back.domain.blog.blog.dto;

import com.back.domain.blog.blog.entity.BlogStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record BlogWriteReqDto(
        @NotBlank(message = "제목을 입력해주세요")
        @Size(max = 50, message = "제목은 50자 이내로 입력해주세요")
        String title,
        @NotBlank(message = "내용을 입력해주세요")
        String content,
        @NotNull
        BlogStatus status,
        @Size(max = 5, message = "해시태그는 최대 5개까지 가능합니다.")
        List<String> hashtagNames
) {
}
