package com.back.domain.recommend.recentview.constants;

public final class GuestConstants {
    private GuestConstants() {
    }

    public static final String GUEST_COOKIE_NAME = "guestId";
    public static final int GUEST_COOKIE_MAX_AGE = 60 * 60 * 24 * 30; // 30일
}
