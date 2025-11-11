package com.back.domain.shorlog.shorloghashtag.entity;

import com.back.domain.shared.hashtag.entity.Hashtag;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(uniqueConstraints = { // 같은 숏로그에 같은 해시태그 중복 방지
        @UniqueConstraint(
                name = "uk_shorlog_hashtag",
                columnNames = {"shorlog_id", "hashtag_id"}
        )
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class ShorlogHashtag extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shorlog_id", nullable = false)
    private Shorlog shorlog;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hashtag_id", nullable = false)
    private Hashtag hashtag;
}