package com.back.global.ut;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtil {

    public static String toRelativeTime(LocalDateTime time) {
        long seconds = Duration.between(time, LocalDateTime.now()).getSeconds();

        if (seconds < 60) return "방금 전";
        long minutes = seconds / 60;

        if (minutes < 60) return minutes + "분 전";
        long hours = minutes / 60;

        if (hours < 24) return hours + "시간 전";
        long days = hours / 24;

        if (days < 7) return days + "일 전";
        long weeks = days / 7;

        if (weeks < 4) return weeks + "주 전";
        long months = days / 30;

        if (months < 12) return months + "개월 전";
        long years = days / 365;

        return years + "년 전";
    }
}
