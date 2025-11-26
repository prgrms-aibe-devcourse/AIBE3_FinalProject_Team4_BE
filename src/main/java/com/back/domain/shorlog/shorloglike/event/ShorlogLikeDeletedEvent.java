package com.back.domain.shorlog.shorloglike.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ShorlogLikeDeletedEvent {
    private Long shorlogId;
}

