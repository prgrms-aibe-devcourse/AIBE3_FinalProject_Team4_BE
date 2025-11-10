package com.back.domain.hashtag.hashtag.entity;

import com.back.domain.blog.hashtag.entity.BlogHashtag;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@Table(name = "hashtags")
public class Hashtag {
    @OneToMany(mappedBy = "hashtag", orphanRemoval = true, cascade = CascadeType.ALL)
    private final List<BlogHashtag> blogHashtag = new ArrayList<>();
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;
    private String name;

}
