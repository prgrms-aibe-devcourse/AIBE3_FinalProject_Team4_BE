package com.back.domain.shared.hashtag.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Hashtag extends BaseEntity {
    @Column(length = 255, nullable = false, unique = true)
    private String name;
}