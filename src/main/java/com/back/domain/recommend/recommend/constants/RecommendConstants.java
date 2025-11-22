package com.back.domain.recommend.recommend.constants;

public final class RecommendConstants {
    private RecommendConstants() {}

    public static final int LIKE_LIMIT_RECENT_COUNT = 20;     // 사용자가 최근 좋아요/북마크한 게시물 중 추천에 사용할 개수
    public static final int COMMENT_LIMIT_RECENT_COUNT = 10; // 사용자가 최근 댓글 단 게시물 개수
    public static final int POST_LIMIT_RECENT_COUNT = 5;     // 사용자가 최근 작성한 게시물 개수

    public static final int LIKE_LOOKBACK_DAYS = 30;
    public static final int COMMENT_LOOKBACK_DAYS = 14;
    public static final int POST_LOOKBACK_DAYS = 90;
}
