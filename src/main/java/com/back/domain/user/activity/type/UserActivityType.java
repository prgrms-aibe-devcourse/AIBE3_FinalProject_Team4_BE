package com.back.domain.user.activity.type;

import lombok.Getter;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Getter
public enum UserActivityType {

    LIKE(
            20,
            3.0f,
            1.0f,
            30
    ),

    COMMENT(
            10,
            2.0f,
            0.8f,
            14
    ),

    POST(
            5,
            1.0f,
            0.5f,
            90
    );

    private final int limit;
    private final float weight;
    private final float penaltyWeight; // 기간(maxDays) 초과한 경우 적용되는 가중치
    private final int maxDays;

    UserActivityType(int limit, float weight, float penaltyWeight, int maxDays) {
        this.limit = limit;
        this.weight = weight;
        this.penaltyWeight = penaltyWeight;
        this.maxDays = maxDays;
    }

    public float getEffectiveWeight(LocalDateTime activityAt) {
        long days = ChronoUnit.DAYS.between(activityAt, LocalDateTime.now());
        return (days > maxDays) ? penaltyWeight : weight;
    }
}


