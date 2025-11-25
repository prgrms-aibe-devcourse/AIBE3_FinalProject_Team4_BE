package com.back.domain.shorlog.shorlogdoc.service;

import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.event.CommentCreatedEvent;
import com.back.domain.comments.comments.event.CommentDeletedEvent;
import com.back.domain.shared.image.entity.Image;
import com.back.domain.shorlog.shorlog.event.ShorlogCreatedEvent;
import com.back.domain.shorlog.shorlog.event.ShorlogDeletedEvent;
import com.back.domain.shorlog.shorlog.event.ShorlogUpdatedEvent;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogimage.repository.ShorlogImagesRepository;
import com.back.domain.shorlog.shorloglike.event.ShorlogLikeCreatedEvent;
import com.back.domain.shorlog.shorloglike.event.ShorlogLikeDeletedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ShorlogDocEventListener {

    private final ShorlogDocService shorlogDocService;
    private final ShorlogRepository shorlogRepository;
    private final ShorlogImagesRepository shorlogImagesRepository;

     // 숏로그 생성 이벤트
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleShorlogCreated(ShorlogCreatedEvent event) {
        reindexShorlog(event.getShorlogId());
    }

     // 숏로그 수정 이벤트
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleShorlogUpdated(ShorlogUpdatedEvent event) {
        reindexShorlog(event.getShorlogId());
    }

     // 숏로그 삭제 이벤트
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleShorlogDeleted(ShorlogDeletedEvent event) {
        shorlogDocService.deleteShorlog(event.getShorlogId());
    }

     // 댓글 생성 이벤트
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentCreated(CommentCreatedEvent event) {
        if (event.getTargetType() == CommentsTargetType.SHORLOG) {
            shorlogDocService.updateElasticsearchCounts(event.getTargetId());
        }
    }

     // 댓글 삭제 이벤트
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCommentDeleted(CommentDeletedEvent event) {
        if (event.getTargetType() == CommentsTargetType.SHORLOG) {
            shorlogDocService.updateElasticsearchCounts(event.getTargetId());
        }
    }

     // 좋아요 생성 이벤트
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeCreated(ShorlogLikeCreatedEvent event) {
        shorlogDocService.updateElasticsearchCounts(event.getShorlogId());
    }

     // 좋아요 삭제 이벤트
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleLikeDeleted(ShorlogLikeDeletedEvent event) {
        shorlogDocService.updateElasticsearchCounts(event.getShorlogId());
    }

     // 숏로그 재인덱싱 (생성/수정 공통 로직)
    private void reindexShorlog(Long shorlogId) {
        shorlogRepository.findByIdWithUser(shorlogId).ifPresent(shorlog -> {
            List<Image> images = shorlogImagesRepository.findAllImagesByShorlogIdOrderBySort(shorlogId);
            String thumbnailUrl = images.isEmpty() ? null : images.get(0).getS3Url();

            shorlogDocService.indexShorlog(
                    shorlog,
                    shorlog.getContent(),
                    thumbnailUrl,
                    shorlog.getUser().getId(),
                    shorlog.getUser().getNickname(),
                    shorlog.getUser().getProfileImgUrl()
            );
        });
    }
}

