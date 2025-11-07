package com.back.project.domain.shared.hashtag.service;

import com.back.project.domain.shared.hashtag.entity.Hashtag;
import com.back.project.domain.shared.hashtag.repository.HashtagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HashtagService {

    private final HashtagRepository hashtagRepository;

    @Transactional
    public Hashtag findOrCreate(String name) {
        String cleanName = name.startsWith("#") ? name.substring(1) : name;
        validateHashtagName(cleanName);

        return hashtagRepository.findByName(cleanName)
                .orElseGet(() -> {
                    Hashtag newHashtag = Hashtag.builder()
                            .name(cleanName)
                            .build();
                    return hashtagRepository.save(newHashtag);
                });
    }

    @Transactional
    public List<Hashtag> findOrCreateAll(List<String> names) {
        return names.stream()
                .map(this::findOrCreate)
                .toList();
    }

    private void validateHashtagName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("해시태그는 비어있을 수 없습니다.");
        }
        if (name.length() < 2) {
            throw new IllegalArgumentException("해시태그는 최소 2글자 이상이어야 합니다.");
        }
        if (name.length() > 255) {
            throw new IllegalArgumentException("해시태그는 최대 255자까지 가능합니다.");
        }
        if (!name.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new IllegalArgumentException("해시태그는 한글, 영문, 숫자만 사용 가능합니다.");
        }
    }

    public Optional<Hashtag> findById(Long id) {
        return hashtagRepository.findById(id);
    }

    public Optional<Hashtag> findByName(String name) {
        String cleanName = name.startsWith("#") ? name.substring(1) : name;
        return hashtagRepository.findByName(cleanName);
    }
}