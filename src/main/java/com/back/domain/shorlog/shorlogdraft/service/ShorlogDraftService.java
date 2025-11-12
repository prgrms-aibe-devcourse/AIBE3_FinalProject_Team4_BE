package com.back.domain.shorlog.shorlogdraft.service;

import com.back.domain.shorlog.shorlogdraft.dto.CreateDraftRequest;
import com.back.domain.shorlog.shorlogdraft.dto.DraftResponse;
import com.back.domain.shorlog.shorlogdraft.entity.ShorlogDraft;
import com.back.domain.shorlog.shorlogdraft.repository.ShorlogDraftRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShorlogDraftService {

    private final ShorlogDraftRepository draftRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    private static final int MAX_DRAFTS = 5;
    private static final int EXPIRY_DAYS = 7;

    @Transactional
    public DraftResponse createDraft(Long userId, CreateDraftRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        int currentDraftCount = draftRepository.countByUserId(userId);
        if (currentDraftCount >= MAX_DRAFTS) {
            throw new IllegalStateException("임시저장은 최대 5개까지 가능합니다.");
        }

        String hashtagsJson = convertHashtagsToJson(request.getHashtags());

        ShorlogDraft draft = ShorlogDraft.builder()
                .user(user)
                .content(request.getContent())
                .hashtags(hashtagsJson)
                .build();

        draft.setThumbnailUrlList(request.getThumbnailUrls());

        ShorlogDraft savedDraft = draftRepository.save(draft);
        return DraftResponse.of(savedDraft, request.getHashtags());
    }

    public List<DraftResponse> getDrafts(Long userId) {
        List<ShorlogDraft> drafts = draftRepository.findByUserIdOrderByCreatedAtDesc(userId);
        return drafts.stream()
                .map(draft -> DraftResponse.of(draft, convertJsonToHashtags(draft.getHashtags())))
                .toList();
    }

    public DraftResponse getDraft(Long userId, Long draftId) {
        ShorlogDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new NoSuchElementException("임시저장을 찾을 수 없습니다."));

        if (!draft.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 임시저장만 조회할 수 있습니다.");
        }

        return DraftResponse.of(draft, convertJsonToHashtags(draft.getHashtags()));
    }

    @Transactional
    public DraftResponse updateDraft(Long userId, Long draftId, CreateDraftRequest request) {
        ShorlogDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new NoSuchElementException("임시저장을 찾을 수 없습니다."));

        if (!draft.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 임시저장만 수정할 수 있습니다.");
        }

        String hashtagsJson = convertHashtagsToJson(request.getHashtags());
        draft.update(request.getContent(), request.getThumbnailUrls(), hashtagsJson);

        return DraftResponse.of(draft, request.getHashtags());
    }

    @Transactional
    public void deleteDraft(Long userId, Long draftId) {
        ShorlogDraft draft = draftRepository.findById(draftId)
                .orElseThrow(() -> new NoSuchElementException("임시저장을 찾을 수 없습니다."));

        if (!draft.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 임시저장만 삭제할 수 있습니다.");
        }

        draftRepository.delete(draft);
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *")
    public void deleteExpiredDrafts() {
        LocalDateTime expiryDate = LocalDateTime.now().minusDays(EXPIRY_DAYS);
        draftRepository.deleteExpiredDrafts(expiryDate);
    }

    private String convertHashtagsToJson(List<String> hashtags) {
        if (hashtags == null || hashtags.isEmpty()) {
            return null;
        }

        try {
            return objectMapper.writeValueAsString(hashtags);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("해시태그 JSON 변환 실패", e);
        }
    }

    private List<String> convertJsonToHashtags(String hashtagsJson) {
        if (hashtagsJson == null || hashtagsJson.isEmpty()) {
            return new ArrayList<>();
        }

        try {
            return objectMapper.readValue(hashtagsJson, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            return new ArrayList<>();
        }
    }
}
