package com.back.domain.comments.comments.service;

import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.comments.comments.dto.CommentCreateRequestDto;
import com.back.domain.comments.comments.dto.CommentResponseDto;
import com.back.domain.comments.comments.dto.CommentUpdateRequestDto;
import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.exception.CommentsErrorCase;
import com.back.domain.comments.comments.repository.CommentsRepository;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
<<<<<<< HEAD
import com.back.domain.shorlog.shorlog.entity.Shorlog;
=======
>>>>>>> d42b2eeb9fdabdd2298889a89ab7d94309e5000b
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final UserRepository userRepository;
    private final BlogRepository blogRepository;
    private final ShorlogRepository shorlogRepository;
    private final NotificationService notificationService;

    // 공통
    private Comments getComment(Long id) {
        return commentsRepository.findById(id)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));
    }

    private Comments getParentComment(Long parentId) {
        return commentsRepository.findById(parentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.PARENT_COMMENT_NOT_FOUND));
    }

    private void checkOwnership(Comments comment, Long userId) {
        if (!comment.getUser().getId().equals(userId)) {
            throw new ServiceException(CommentsErrorCase.COMMENT_FORBIDDEN);
        }
    }

    @Transactional
    public RsData<CommentResponseDto> createComment(Long userId, CommentCreateRequestDto req) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.USER_NOT_FOUND));

        // target 존재 여부 검증
        if (!req.targetType().exists(req.targetId(),blogRepository, shorlogRepository)) {
            throw new ServiceException(CommentsErrorCase.TARGET_NOT_FOUND);
        }

        Comments parent = null;
        if (req.parentId() != null && req.parentId() != 0) {
            parent = getParentComment(req.parentId());
        }

        Comments comment = Comments.builder()
                .targetType(req.targetType())
                .targetId(req.targetId())
                .user(user)
                .content(req.content())
                .parent(parent)
                .build();

        commentsRepository.save(comment);

        // ===========================
        //       🔔 알림 처리 로직
        // ===========================

        // (1) 대댓글인 경우 → 부모 댓글 작성자에게 알림
        if (parent != null) {
            Long receiverId = parent.getUser().getId();

            // 자기 댓글에 대댓글 달면 알림 X
            if (!receiverId.equals(userId)) {

                notificationService.send(
                        receiverId,
                        userId,
                        NotificationType.COMMENT_REPLY,
                        comment.getId(),          // target = commentId
                        user.getNickname()
                );
            }
        }
        // (2) 일반 댓글: targetType → 게시글/쇼로그 작성자에게 알림
        else {
            Long receiverId = null;
            NotificationType type = null;

            switch (req.targetType()) {
                case BLOG -> {
                    Blog blog = blogRepository.findById(req.targetId())
                            .orElseThrow(() -> new RuntimeException("블로그 게시글을 찾을 수 없습니다."));
                    receiverId = blog.getUser().getId();
                    type = NotificationType.BLOG_COMMENT;
                }
                case SHORLOG -> {
                    Shorlog sl = shorlogRepository.findById(req.targetId())
                            .orElseThrow(() -> new RuntimeException("쇼로그를 찾을 수 없습니다."));
                    receiverId = sl.getUser().getId();
                    type = NotificationType.SHORLOG_COMMENT;
                }
            }

            // 본인 글에 단 댓글이면 알림 X
            if (!receiverId.equals(userId)) {
                notificationService.send(
                        receiverId,
                        userId,
                        type,
                        req.targetId(),          // target = postId
                        user.getNickname()
                );
            }
        }

        return RsData.of(
                "200-1",
                "댓글이 등록되었습니다.",
                CommentResponseDto.fromEntity(comment)
        );
    }


    // 댓글 조회
    @Transactional(readOnly = true)
    public RsData<List<CommentResponseDto>> getCommentsByTarget(
            CommentsTargetType targetType,
            Long targetId
    ) {

        List<Comments> comments = commentsRepository
                .findByTargetTypeAndTargetIdAndParentIsNullOrderByCreatedAtAsc(targetType, targetId);

        return RsData.of("200-1", "댓글 목록 조회 성공",
                comments.stream()
                        .map(CommentResponseDto::fromEntity)
                        .toList()
        );
    }

    // 댓글 수정
    @Transactional
    public RsData<CommentResponseDto> updateComment(
            Long commentId,
            Long userId,
            CommentUpdateRequestDto req
    ) {

        Comments comment = getComment(commentId);
        checkOwnership(comment, userId);

        comment.updateContent(req.content());

        return RsData.of(
                "200-2",
                "댓글이 수정되었습니다.",
                CommentResponseDto.fromEntity(comment)
        );
    }

    // 댓글 삭제
    @Transactional
    public RsData<Void> deleteComment(Long commentId, Long userId) {

        Comments comment = getComment(commentId);
        checkOwnership(comment, userId);

        commentsRepository.delete(comment);

        return RsData.of("200-3", "댓글이 삭제되었습니다.", null);
    }

    // 댓글 좋아요
    @Transactional
    public RsData<CommentResponseDto> likeComment(Long commentId, Long userId) {

        Comments comment = getComment(commentId);

        if (comment.getUser().getId().equals(userId)) {
            throw new ServiceException(CommentsErrorCase.COMMENT_LIKE_FORBIDDEN);
        }
        if (comment.getLikedUserIds().contains(userId)) {
            throw new ServiceException(CommentsErrorCase.COMMENT_LIKE_ALREADY_EXISTS);
        }

        comment.addLike(userId);

        return RsData.of(
                "200-4",
                "댓글에 좋아요를 눌렀습니다.",
                CommentResponseDto.fromEntity(comment)
        );
    }

    // 댓글 좋아요 취소
    @Transactional
    public RsData<CommentResponseDto> unlikeComment(Long commentId, Long userId) {

        Comments comment = getComment(commentId);

        comment.removeLike(userId);

        return RsData.of(
                "200-5",
                "댓글 좋아요를 취소했습니다.",
                CommentResponseDto.fromEntity(comment)
        );
    }

    public List<CommentResponseDto> getCommentsByType(Long blogId, CommentsTargetType targetType) {
        List<Comments> comment = commentsRepository
                .findByTargetTypeAndTargetIdAndParentIsNullOrderByCreatedAtAsc(targetType, blogId);
        return comment.stream()
                .map(CommentResponseDto::fromEntity)
                .toList();
    }

    public Map<Long, Long> getCommentCounts(List<Long> targetIds, CommentsTargetType targetType) {
        return commentsRepository.countByTargetIdsAndType(targetIds, targetType)
                .stream()
                .collect(Collectors.toMap(
                        row -> (Long) row[0],  // targetId
                        row -> (Long) row[1]   // count
                ));
    }
}
