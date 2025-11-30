package com.back.domain.comments.comments.controller;

import com.back.domain.comments.comments.dto.CommentCreateRequestDto;
import com.back.domain.comments.comments.dto.CommentResponseDto;
import com.back.domain.comments.comments.dto.CommentUpdateRequestDto;
import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.service.CommentsService;
import com.back.global.config.security.SecurityUser;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comment API", description = "댓글 및 대댓글 관련 API")
public class ApiV1CommentsController {

    private final CommentsService commentsService;

    /**
     * 댓글 작성
     */
    @PostMapping
    @Operation(
            summary = "댓글 작성",
            description = "게시글에 댓글 또는 대댓글을 작성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 작성 성공",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
    public RsData<CommentResponseDto> createComment(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody CommentCreateRequestDto req
    ) {
        return commentsService.createComment(user.getId(), req);
    }

    /**
     * 댓글 조회
     */
    @GetMapping("/{targetType}/{targetId}")
    @Operation(
            summary = "댓글 조회",
            description = "게시글의 모든 댓글 및 대댓글을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
                    @ApiResponse(responseCode = "404", description = "게시글을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
    public RsData<List<CommentResponseDto>> getCommentsByTarget(
            @PathVariable CommentsTargetType targetType,
            @PathVariable Long targetId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        Long currentUserId = (user != null) ? user.getId() : null;
        return commentsService.getCommentsByTarget(targetType, targetId, currentUserId);
    }

    /**
     * 댓글 수정
     */
    @PutMapping("/{commentId}")
    @Operation(
            summary = "댓글 수정",
            description = "본인 댓글을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
    public RsData<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody CommentUpdateRequestDto req
    ) {
        return commentsService.updateComment(commentId, user.getId(), req);
    }

    /**
     * 댓글 삭제
     */
    @DeleteMapping("/{commentId}")
    @Operation(
            summary = "댓글 삭제",
            description = "본인 댓글을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
    public RsData<Void> deleteComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        return commentsService.deleteComment(commentId, user.getId());
    }

    /**
     * 좋아요 추가
     */
    @PostMapping("/{commentId}/like")
    @Operation(
            summary = "댓글 좋아요",
            description = "특정 댓글에 좋아요를 추가합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 성공",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
    public RsData<CommentResponseDto> likeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        return commentsService.likeComment(commentId, user.getId());
    }

    /**
     * 좋아요 취소
     */
    @PostMapping("/{commentId}/unlike")
    @Operation(
            summary = "댓글 좋아요 취소",
            description = "특정 댓글의 좋아요를 취소합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "좋아요 취소 성공",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
                            content = @Content(schema = @Schema(implementation = RsData.class))),
                    @ApiResponse(responseCode = "404", description = "댓글을 찾을 수 없음",
                            content = @Content(schema = @Schema(implementation = RsData.class)))
            }
    )
    public RsData<CommentResponseDto> unlikeComment(
            @PathVariable Long commentId,
            @AuthenticationPrincipal SecurityUser user
    ) {
        return commentsService.unlikeComment(commentId, user.getId());
    }
}
