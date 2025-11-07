package com.back.project.domain.shorlog.shorlog.service;

import com.back.project.domain.shared.hashtag.entity.Hashtag;
import com.back.project.domain.shared.hashtag.service.HashtagService;
import com.back.project.domain.shorlog.shorlog.dto.CreateShorlogRequest;
import com.back.project.domain.shorlog.shorlog.dto.CreateShorlogResponse;
import com.back.project.domain.shorlog.shorlog.entity.Shorlog;
import com.back.project.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.project.domain.shorlog.shorloghashtag.entity.ShorlogHashtag;
import com.back.project.domain.shorlog.shorloghashtag.repository.ShorlogHashtagRepository;
import com.back.project.domain.user.user.entity.User;
import com.back.project.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShorlogService {

    private final ShorlogRepository shorlogRepository;
    private final ShorlogHashtagRepository shorlogHashtagRepository;
    private final HashtagService hashtagService;
    private final UserRepository userRepository;

    private static final int MAX_HASHTAGS = 10;

    @Transactional
    public CreateShorlogResponse createShorlog(Long userId, CreateShorlogRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException());

        Shorlog shorlog = Shorlog.builder()
                .user(user)
                .content(request.getContent())
                .thumbnailUrl(request.getThumbnailUrl())
                .thumbnailType(request.getThumbnailType())
                .viewCount(0)
                .build();

        Shorlog savedShorlog = shorlogRepository.save(shorlog);
        List<String> hashtagNames = saveHashtags(savedShorlog, request.getHashtags());

        return CreateShorlogResponse.from(savedShorlog, hashtagNames);
    }

    private List<String> saveHashtags(Shorlog shorlog, List<String> hashtagNames) {
        if (hashtagNames == null || hashtagNames.isEmpty()) {
            return List.of();
        }

        List<String> uniqueHashtags = hashtagNames.stream()
                .distinct()
                .limit(MAX_HASHTAGS)
                .toList();

        List<Hashtag> hashtags = hashtagService.findOrCreateAll(uniqueHashtags);

        List<ShorlogHashtag> shorlogHashtags = hashtags.stream()
                .map(hashtag -> ShorlogHashtag.builder()
                        .shorlog(shorlog)
                        .hashtag(hashtag)
                        .build())
                .toList();

        shorlogHashtagRepository.saveAll(shorlogHashtags);

        return hashtags.stream()
                .map(Hashtag::getName)
                .toList();
    }
}






