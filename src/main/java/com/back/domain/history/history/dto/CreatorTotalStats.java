package com.back.domain.history.history.dto;

public record CreatorTotalStats(
        long views,
        long likes,
        long bookmarks,
        long comments,
        long followerCount
) {
}