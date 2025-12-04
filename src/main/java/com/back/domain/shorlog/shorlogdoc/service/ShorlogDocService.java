package com.back.domain.shorlog.shorlogdoc.service;

import com.back.domain.comments.comments.entity.CommentsTargetType;
import com.back.domain.comments.comments.repository.CommentsRepository;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogdoc.document.ShorlogDoc;
import com.back.domain.shorlog.shorlogdoc.repository.ShorlogDocRepository;
import com.back.domain.shorlog.shorloghashtag.repository.ShorlogHashtagRepository;
import com.back.domain.shorlog.shorloglike.repository.ShorlogLikeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShorlogDocService {

    private final ShorlogDocRepository shorlogDocRepository;
    private final ShorlogHashtagRepository shorlogHashtagRepository;
    private final ShorlogLikeRepository shorlogLikeRepository;
    private final CommentsRepository commentsRepository;
    private final ShorlogRepository shorlogRepository;

     // Elasticsearch에 숏로그 인덱싱
    @Transactional
    public void indexShorlog(Shorlog shorlog, String content, String thumbnailUrl, Long userId, String nickname, String profileImgUrl) {
        List<String> hashtags = shorlogHashtagRepository.findHashtagNamesByShorlogId(shorlog.getId());

        int likeCount = (int) shorlogLikeRepository.countByShorlog(shorlog);
        int viewCount = shorlog.getViewCount();

        // 댓글 수 조회
        Long commentCountLong = commentsRepository.countByTargetTypeAndTargetId(
                CommentsTargetType.SHORLOG,
                shorlog.getId()
        );
        int commentCount = commentCountLong != null ? commentCountLong.intValue() : 0;

        // 인기도 점수 계산 (viewCount + likeCount * 2)
        int popularityScore = viewCount + (likeCount * 2);

        ShorlogDoc doc = ShorlogDoc.builder()
                .id(shorlog.getId().toString())
                .userId(userId)  // 직접 전달받은 값 사용
                .nickname(nickname)
                .profileImgUrl(profileImgUrl)
                .content(content)
                .thumbnailUrl(thumbnailUrl)  // 직접 전달받은 URL 사용
                .hashtags(hashtags)
                .viewCount(viewCount)
                .likeCount(likeCount)
                .commentCount(commentCount)
                .popularityScore(popularityScore)
                .createdAt(shorlog.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())
                .build();

        shorlogDocRepository.save(doc);
    }

    @Transactional
    public void deleteShorlog(Long shorlogId) {
        shorlogDocRepository.deleteById(shorlogId.toString());
    }

    public Page<ShorlogDoc> searchShorlogs(String keyword, String sort, int page, int size) {
        Sort sortOption = getSortOption(sort);
        Pageable pageable = PageRequest.of(page, size, sortOption);
        return shorlogDocRepository.searchByKeywordOrHashtag(keyword, pageable);
    }

    private Sort getSortOption(String sort) {
        return switch (sort) {
            case "latest" -> Sort.by(Sort.Direction.DESC, "createdAt");
            case "popular" -> Sort.by(Sort.Direction.DESC, "popularityScore");
            case "views" -> Sort.by(Sort.Direction.DESC, "viewCount");
            default -> Sort.by(Sort.Direction.DESC, "createdAt");
        };
    }

    // Elasticsearch 카운트 통합 업데이트 (조회수, 좋아요 수, 댓글 수)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateElasticsearchCounts(Long shorlogId) {
        shorlogDocRepository.findById(shorlogId.toString()).ifPresent(doc -> {
            shorlogRepository.findById(shorlogId).ifPresent(shorlog -> {
                // MySQL에서 최신 조회수 조회
                int viewCount = shorlog.getViewCount();
                int likeCount = (int) shorlogLikeRepository.countByShorlog(shorlog);

                Long commentCountLong = commentsRepository.countByTargetTypeAndTargetId(
                        CommentsTargetType.SHORLOG,
                        shorlogId
                );
                int commentCount = commentCountLong != null ? commentCountLong.intValue() : 0;

                int popularityScore = viewCount + (likeCount * 2);

                ShorlogDoc updatedDoc = ShorlogDoc.builder()
                        .id(doc.getId())
                        .userId(doc.getUserId())
                        .nickname(doc.getNickname())
                        .profileImgUrl(doc.getProfileImgUrl())
                        .content(doc.getContent())
                        .thumbnailUrl(doc.getThumbnailUrl())
                        .hashtags(doc.getHashtags())
                        .viewCount(viewCount)
                        .likeCount(likeCount)
                        .commentCount(commentCount)
                        .popularityScore(popularityScore)
                        .createdAt(doc.getCreatedAt())
                        .build();

                shorlogDocRepository.save(updatedDoc);
            });
        });
    }

    // 사용자 프로필 정보 업데이트 (닉네임, 프로필 이미지)
    @Transactional
    public void updateUserProfileInShorlogs(Long userId, String newNickname, String newProfileImgUrl) {
        // 해당 사용자의 모든 숏로그 문서 조회
        List<ShorlogDoc> userShorlogs = shorlogDocRepository.findByUserId(userId);

        // 각 문서의 닉네임과 프로필 이미지 업데이트
        userShorlogs.forEach(doc -> {
            ShorlogDoc updatedDoc = ShorlogDoc.builder()
                    .id(doc.getId())
                    .userId(doc.getUserId())
                    .nickname(newNickname)
                    .profileImgUrl(newProfileImgUrl)
                    .content(doc.getContent())
                    .thumbnailUrl(doc.getThumbnailUrl())
                    .hashtags(doc.getHashtags())
                    .viewCount(doc.getViewCount())
                    .likeCount(doc.getLikeCount())
                    .commentCount(doc.getCommentCount())
                    .popularityScore(doc.getPopularityScore())
                    .createdAt(doc.getCreatedAt())
                    .build();

            shorlogDocRepository.save(updatedDoc);
        });
    }
}




