package com.back.domain.comments.comments.service;

import com.back.domain.comments.comments.dto.*;
import com.back.domain.comments.comments.entity.Comments;
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

    @Transactional
    public RsData<CommentResponseDto> createComment(CommentCreateRequestDto req) {
        Comments parent = null;
        if (req.parentId() != null) {
            parent = commentsRepository.findById(req.parentId())
                    .orElseThrow(() -> new ServiceException(CommentsErrorCase.PARENT_COMMENT_NOT_FOUND));
        }

        Comments comment = Comments.builder()
                .postId(req.postId())
                .userId(req.userId())
                .content(req.content())
                .parent(parent)
                .build();

        commentsRepository.save(comment);
        return RsData.successOf(CommentResponseDto.fromEntity(comment));
    }

    @Transactional(readOnly = true)
    public RsData<List<CommentResponseDto>> getCommentsByPost(Long postId) {
        List<Comments> comments = commentsRepository.findByPostIdAndParentIsNullOrderByCreatedAtAsc(postId);
        List<CommentResponseDto> dtos = comments.stream()
                .map(CommentResponseDto::fromEntity)
                .toList();
        return RsData.successOf(dtos);
    }

    @Transactional
    public RsData<CommentResponseDto> updateComment(Long commentId, Long userId, CommentUpdateRequestDto req) {
        Comments comments = commentsRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        if (!comments.getUserId().equals(userId)) {
            throw new ServiceException(CommentsErrorCase.UNAUTHORIZED_UPDATE);
        }

        comments.updateContent(req.content());
        return RsData.successOf(CommentResponseDto.fromEntity(comments));
    }

    @Transactional
    public RsData<Void> deleteComment(Long commentsId, Long userId) {
        Comments comments = commentsRepository.findById(commentsId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        if (!comments.getUserId().equals(userId)) {
            throw new ServiceException(CommentsErrorCase.UNAUTHORIZED_DELETE);
        }

        commentsRepository.delete(comments);
        return new RsData<>("200-3", "댓글이 삭제되었습니다.");
    }

    @Transactional
    public RsData<CommentResponseDto> likeComment(Long commentId, Long userId) {
        Comments comments = commentsRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        comments.addLike(userId);
        commentsRepository.save(comments);

        return RsData.successOf(CommentResponseDto.fromEntity(comments));
    }

    @Transactional
    public RsData<CommentResponseDto> unlikeComment(Long commentId, Long userId) {
        Comments comments = commentsRepository.findById(commentId)
                .orElseThrow(() -> new ServiceException(CommentsErrorCase.COMMENT_NOT_FOUND));

        comments.removeLike(userId);
        commentsRepository.save(comments);

        return RsData.successOf(CommentResponseDto.fromEntity(comments));
    }
}
