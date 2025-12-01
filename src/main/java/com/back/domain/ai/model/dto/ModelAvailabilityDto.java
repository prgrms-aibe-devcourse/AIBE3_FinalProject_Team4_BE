package com.back.domain.ai.model.dto;

public record ModelAvailabilityDto(
        Long id,
        String name,
        boolean available
) {
}
