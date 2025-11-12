package com.back.domain.shared.hashtag.entity;

import com.back.domain.blog.bloghashtag.entity.BlogHashtag;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Table(name = "hashtags")
public class Hashtag extends BaseEntity {
    @OneToMany(mappedBy = "hashtag", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<BlogHashtag> blogHashtag = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    @Column(length = 255, nullable = false, unique = true)
    private String name;
}