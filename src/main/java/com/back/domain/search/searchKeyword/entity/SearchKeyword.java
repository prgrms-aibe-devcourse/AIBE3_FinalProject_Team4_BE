package com.back.domain.search.searchKeyword.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class SearchKeyword extends BaseEntity {

    @Column(unique = true, nullable = false)
    private String keyword;

    @Column(nullable = false)
    private Long searchCount = 0L;
}
