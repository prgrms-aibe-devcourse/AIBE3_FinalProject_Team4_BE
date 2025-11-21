package com.back.domain.shorlog.shorlog.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ShorlogDeletedEvent {
    private final Long shorlogId;
}

