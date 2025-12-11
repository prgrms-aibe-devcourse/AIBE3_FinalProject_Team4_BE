package com.back.domain.history.history.dto;

public record CreatorPeriodStats(
        long views,
        long likes,
        long bookmarks,
        long comments,
        long followers
) {
}