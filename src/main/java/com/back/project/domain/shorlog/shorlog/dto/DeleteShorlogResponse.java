package com.back.project.domain.shorlog.shorlog.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeleteShorlogResponse {
    private Long deletedId;

    public static DeleteShorlogResponse of(Long id) {
        return DeleteShorlogResponse.builder()
                .deletedId(id)
                .build();
    }
}

