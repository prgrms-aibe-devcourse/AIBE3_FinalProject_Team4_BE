package com.back.domain.comments.comments.service;

import com.back.domain.comments.comments.dto.CommentCreateRequestDto;
import com.back.domain.comments.comments.dto.CommentResponseDto;
import com.back.domain.comments.comments.dto.CommentUpdateRequestDto;
import com.back.domain.comments.comments.entity.Comments;
import com.back.domain.comments.comments.repository.CommentsRepository;
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
                    .orElseThrow(() -> new IllegalArgumentException("부모 댓글을 찾을 수 없습니다."));
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
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comments.getUserId().equals(userId)) {
            return RsData.failOf("본인 댓글만 수정할 수 있습니다.");
        }

        comments.updateContent(req.content());
        return RsData.successOf(CommentResponseDto.fromEntity(comments));
    }

    @Transactional
    public RsData<Void> deleteComment(Long commentsId, Long userId) {
        Comments comments = commentsRepository.findById(commentsId)
                .orElseThrow(() -> new IllegalArgumentException("댓글을 찾을 수 없습니다."));

        if (!comments.getUserId().equals(userId)) {
            return RsData.failOf("본인 댓글만 삭제할 수 있습니다.");
        }

        commentsRepository.delete(comments);
        return new RsData<>("200-3", "댓글이 삭제되었습니다.");
    }
}
