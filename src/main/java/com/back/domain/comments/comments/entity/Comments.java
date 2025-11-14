package com.back.domain.comments.comments.entity;

import com.back.domain.user.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comments extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CommentsTargetType targetType; // 댓글 대상 (BLOG / SHORLOG 등)

    @Column(nullable = false)
    private Long targetId; // 대상 엔티티의 ID (blogId나 shorlogId)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 500)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Comments parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comments> children = new ArrayList<>();

    @ElementCollection
    @Builder.Default
    private Set<Long> likedUserIds = new HashSet<>();

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
