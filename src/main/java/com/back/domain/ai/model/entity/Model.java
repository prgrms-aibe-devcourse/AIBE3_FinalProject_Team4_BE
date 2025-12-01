package com.back.domain.ai.model.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "models")
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Model extends BaseEntity {

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "limit_count", nullable = false, columnDefinition = "int unsigned")
    private int limitCount;
}
