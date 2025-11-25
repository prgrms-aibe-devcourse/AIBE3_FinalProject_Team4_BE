package com.back.domain.search.searchHistory.entity;

import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class SearchHistory extends BaseEntity {

    private String keyword;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    public void updateTimestamp() {
        this.modifiedAt = LocalDateTime.now();
    }
}
