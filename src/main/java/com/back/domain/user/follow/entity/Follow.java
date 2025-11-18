package com.back.domain.user.follow.entity;

import com.back.domain.user.user.entity.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Table(name = "follow", uniqueConstraints = {@UniqueConstraint(columnNames = {"follower_id", "following_id"})})
public class Follow{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    private User follower;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "following_id")
    private User following;

    @CreatedDate
    private LocalDateTime createdAt;


    private Follow(User follower, User following) {
        this.follower = follower;
        this.following = following;
    }

    public static Follow create(User follower, User following) {
        return new Follow(follower, following);
    }

}
