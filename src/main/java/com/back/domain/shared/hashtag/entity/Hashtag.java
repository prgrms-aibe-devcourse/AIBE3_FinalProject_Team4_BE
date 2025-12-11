package com.back.domain.shared.hashtag.entity;

import com.back.domain.blog.bloghashtag.entity.BlogHashtag;
import com.back.domain.shorlog.shorloghashtag.entity.ShorlogHashtag;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "hashtags")
public class Hashtag extends BaseEntity {
    @OneToMany(mappedBy = "hashtag", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<BlogHashtag> blogHashtag = new ArrayList<>();

    @OneToMany(mappedBy = "hashtag", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<ShorlogHashtag> shorlogHashtag = new ArrayList<>();

    @Column(length = 50, nullable = false, unique = true)
    private String name;
}