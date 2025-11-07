package com.back.project.domain.user.user.entity;

import com.back.project.global.jpa.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {
    private String username;
    private String email;
    private String password;
    private String profileimgurl;

    @Column(name = "tts_token", nullable = false, columnDefinition = "INT DEFAULT 100")
    @Builder.Default
    private Integer ttsToken = 100;

    @Column(name = "tts_last_reset", nullable = false)
    @Builder.Default
    private LocalDateTime ttsLastReset = LocalDateTime.now();
}