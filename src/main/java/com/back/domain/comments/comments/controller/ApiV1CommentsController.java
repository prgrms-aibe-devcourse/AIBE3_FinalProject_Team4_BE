package com.back.domain.comments.comments.controller;

import com.back.domain.comments.comments.dto.*;
import com.back.domain.comments.comments.service.CommentsService;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/comments")
@RequiredArgsConstructor
@Tag(name = "Comment API", description = "댓글 및 대댓글 관련 API")
public class ApiV1CommentsController {

    private final CommentsService commentsService;

    @PostMapping
    @Operation(
            summary = "댓글 작성",
            description = "게시글에 댓글 또는 대댓글을 작성합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "댓글 작성 성공",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터")
            }
    )
    public RsData<CommentResponseDto> createComment(@Valid @RequestBody CommentCreateRequestDto req) {
        return commentsService.createComment(req);
    }

    @GetMapping("/{postId}")
    @Operation(
            summary = "댓글 조회",
            description = "게시글의 모든 댓글 및 대댓글을 조회합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "조회 성공",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class)))
            }
    )
    public RsData<List<CommentResponseDto>> getCommentsByPost(@PathVariable Long postId) {
        return commentsService.getCommentsByPost(postId);
    }

    @PutMapping("/{commentId}")
    @Operation(
            summary = "댓글 수정",
            description = "본인 댓글을 수정합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "수정 성공",
                            content = @Content(schema = @Schema(implementation = CommentResponseDto.class))),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    public RsData<CommentResponseDto> updateComment(
            @PathVariable Long commentId,
            @RequestParam Long userId,
            @Valid @RequestBody CommentUpdateRequestDto req
    ) {
        return commentsService.updateComment(commentId, userId, req);
    }

    @DeleteMapping("/{commentId}")
    @Operation(
            summary = "댓글 삭제",
            description = "본인 댓글을 삭제합니다.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "삭제 성공"),
                    @ApiResponse(responseCode = "403", description = "권한 없음")
            }
    )
    public RsData<Void> deleteComment(
            @PathVariable Long commentId,
            @RequestParam Long userId
    ) {
        return commentsService.deleteComment(commentId, userId);
    }
}
