package com.back.domain.shorlog.shorlog.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ShorlogUpdatedEvent {
    private final Long shorlogId;
}

