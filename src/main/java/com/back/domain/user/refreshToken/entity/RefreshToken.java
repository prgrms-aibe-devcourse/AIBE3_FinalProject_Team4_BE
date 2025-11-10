package com.back.domain.user.refreshToken.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Date;

@Entity
@Getter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private String token;
    private Date expireDate;

    public RefreshToken(Long userId, String token, Date expireDate) {
        this.userId = userId;
        this.token = token;
        this.expireDate = expireDate;
    }

    public boolean isExpired() {
        return new Date().after(this.expireDate);
    }
}
