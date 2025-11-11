package com.back.domain.comments.comments.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comments {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long postId; // Post 엔티티 없이 대체

    @Column(nullable = false)
    private Long userId; // User 엔티티 없이 대체

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comments parent; // 부모 댓글

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comments> children = new ArrayList<>(); // 대댓글 리스트

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    @ElementCollection
    private Set<Long> likedUserIds = new HashSet<>();

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = createdAt;
    }

    @PreUpdate
    public void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void updateContent(String newContent) {
        this.content = newContent;
    }

    public void addLike(Long userId) {
        likedUserIds.add(userId);
    }

    public void removeLike(Long userId) {
        likedUserIds.remove(userId);
    }

    public int getLikeCount() {
        return likedUserIds.size();
    }
}
