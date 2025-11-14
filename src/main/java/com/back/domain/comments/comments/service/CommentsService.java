package com.back.domain.comments.comments.service;

import com.back.domain.comments.comments.dto.CommentCreateRequestDto;
import com.back.domain.comments.comments.dto.CommentResponseDto;
import com.back.domain.comments.comments.dto.CommentUpdateRequestDto;
import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.exception.CommentsErrorCase;
import com.back.domain.comments.comments.repository.CommentsRepository;
import com.back.domain.notification.entity.NotificationType;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.rsData.RsData;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentsService {

    private final CommentsRepository commentsRepository;
    private final NotificationService notificationService;
    private final UserRepository userRepository;

    // 공통 메서드
    private Comments getComment(Long id) {
        return commentsRepository.findById(id)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));
    }

    private Comments getParentComment(Long parentId) {
        return commentsRepository.findById(parentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.PARENT_COMMENT_NOT_FOUND));
    }

    private void checkOwnership(Comments comment, Long userId) {
        if (!comment.getUserId().equals(userId)) {
            throw new ServiceException(CommentsErrorCase.COMMENT_FORBIDDEN);
        }
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    }

    // 댓글 생성
    @Transactional
    public RsData<CommentResponseDto> createComment(CommentCreateRequestDto req) {

        Comments parent = null;
        if (req.parentId() != null) {
            parent = getParentComment(req.parentId());
        }

        Comments comment = Comments.builder()
                .targetType(req.targetType())
                .targetId(req.targetId())
                .userId(req.userId())
                .content(req.content())
                .parent(parent)
                .build();

        commentsRepository.save(comment);

        //알림 생성

        User sender = getUser(req.userId());
        String senderNickname = sender.getNickname();

        // 부모가 있으면 → 대댓글 알림
        if (parent != null && !parent.getUserId().equals(req.userId())) {
            notificationService.send(
                    parent.getUserId(),          // receiver
                    req.userId(),               // sender
                    NotificationType.REPLY,     // type
                    comment.getId(),            // target
                    senderNickname
            );
        }

        // 부모가 없으면 → 일반 댓글 알림
        if (parent == null) {
            // 🔥 target 게시글 작성자에게 알림 보내기
            notificationService.send(
                    comment.getUserId(),         // receiver (게시글 작성자)
                    req.userId(),                // sender
                    NotificationType.COMMENT,    // type
                    comment.getId(),             // target
                    senderNickname
            );
        }

        return RsData.of(
                "200-1",
                "댓글이 등록되었습니다.",
                CommentResponseDto.fromEntity(comment)
        );
    }

    // 댓글 조회
    @Transactional(readOnly = true)
    public RsData<List<CommentResponseDto>> getCommentsByTarget(CommentsTargetType targetType, Long targetId) {

        List<Comments> comments = commentsRepository
                .findByTargetTypeAndTargetIdAndParentIsNullOrderByCreatedAtAsc(targetType, targetId);

        List<CommentResponseDto> dtoList = comments.stream()
                .map(CommentResponseDto::fromEntity)
                .toList();

        return RsData.of("200-1", "댓글 목록 조회 성공", dtoList);
    }

    // 댓글 수정
    @Transactional
    public RsData<CommentResponseDto> updateComment(Long commentId, Long userId, CommentUpdateRequestDto req) {

        Comments comment = getComment(commentId);
        checkOwnership(comment, userId);

        comment.updateContent(req.content());   // JPA dirty checking

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

        // 자기 댓글 좋아요 금지
        if (comment.getUserId().equals(userId)) {
            throw new ServiceException(CommentsErrorCase.COMMENT_LIKE_FORBIDDEN);
        }

        // 중복 좋아요 방지
        if (comment.getLikedUserIds().contains(userId)) {
            throw new ServiceException(CommentsErrorCase.COMMENT_LIKE_ALREADY_EXISTS);
        }

        comment.addLike(userId);

        // 좋아요 알림

        User sender = getUser(userId);

        notificationService.send(
                comment.getUserId(),             // receiver: 댓글 작성자
                userId,                          // sender
                NotificationType.LIKE_COMMENT,   // type
                comment.getId(),                 // target
                sender.getNickname()
        );

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
}
