package com.back.domain.history.history.dto;

import java.time.LocalDate;

public record DailyContentViewsDto(
        LocalDate date,
        long blogViews,
        long shorlogViews
) {
}