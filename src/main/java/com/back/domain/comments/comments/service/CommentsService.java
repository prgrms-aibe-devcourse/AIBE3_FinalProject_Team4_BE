package com.back.domain.comments.comments.service;

import com.back.domain.comments.comments.dto.CommentCreateRequestDto;
import com.back.domain.comments.comments.dto.CommentResponseDto;
import com.back.domain.comments.comments.dto.CommentUpdateRequestDto;
import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.exception.CommentsErrorCase;
import com.back.domain.comments.comments.repository.CommentsRepository;
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

    // 댓글 등록
    @Transactional
    public RsData<CommentResponseDto> createComment(CommentCreateRequestDto req) {
        Comments parent = null;

        if (req.parentId() != null) {
            parent = commentsRepository.findById(req.parentId())
                    .orElseThrow(() -> new ServiceException(CommentsErrorCase.PARENT_COMMENT_NOT_FOUND));
        }

        Comments comment = Comments.builder()
                .targetType(req.targetType())
                .targetId(req.targetId())
                .userId(req.userId())
                .content(req.content())
                .parent(parent)
                .build();

        commentsRepository.save(comment);

        return RsData.of("200-1", "댓글이 등록되었습니다.", CommentResponseDto.fromEntity(comment));
    }
    // 특정 게시글의 댓글 목록 조회
    @Transactional(readOnly = true)
    public RsData<List<CommentResponseDto>> getCommentsByTarget(CommentsTargetType targetType, Long targetId) {
        List<Comments> comments = commentsRepository.findByTargetTypeAndTargetIdAndParentIsNullOrderByCreatedAtAsc(
                targetType,
                targetId
        );

        List<CommentResponseDto> dtoList = comments.stream()
                .map(CommentResponseDto::fromEntity)
                .toList();

        return RsData.of("200-1", "댓글 목록 조회 성공", dtoList);
    }

    // 댓글 수정
    @Transactional
    public RsData<CommentResponseDto> updateComment(Long commentId, Long userId, CommentUpdateRequestDto req) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new ServiceException(CommentsErrorCase.UNAUTHORIZED_UPDATE);
        }

        comment.updateContent(req.content());
        return RsData.of("200-2", "댓글이 수정되었습니다.", CommentResponseDto.fromEntity(comment));
    }

    //  댓글 삭제
    @Transactional
    public RsData<Void> deleteComment(Long commentId, Long userId) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        if (!comment.getUserId().equals(userId)) {
            throw new ServiceException(CommentsErrorCase.UNAUTHORIZED_DELETE);
        }

        commentsRepository.delete(comment);
        return RsData.of("200-3", "댓글이 삭제되었습니다.", null);
    }

    //  댓글 좋아요
    @Transactional
    public RsData<CommentResponseDto> likeComment(Long commentId, Long userId) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        comment.addLike(userId);
        commentsRepository.save(comment);

        return RsData.of("200-4", "댓글에 좋아요를 눌렀습니다.", CommentResponseDto.fromEntity(comment));
    }

    // ✅ 댓글 좋아요 취소
    @Transactional
    public RsData<CommentResponseDto> unlikeComment(Long commentId, Long userId) {
        Comments comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        comment.removeLike(userId);
        commentsRepository.save(comment);

        return RsData.of("200-5", "댓글 좋아요를 취소했습니다.", CommentResponseDto.fromEntity(comment));
    }
}
