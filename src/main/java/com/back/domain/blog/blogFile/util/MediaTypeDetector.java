package com.back.domain.blog.blogFile.util;

import com.back.domain.blog.blogFile.entity.MediaKind;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MediaTypeDetector {

    private static final List<String> IMAGE_EXT = List.of("jpg", "jpeg", "png", "webp");
    private static final List<String> VIDEO_EXT = List.of("mp4", "mov", "webm", "avi");

    public MediaKind detectKind(String extension) {
        String ext = extension.toLowerCase();
        if (IMAGE_EXT.contains(ext)) return MediaKind.IMAGE;
        if (VIDEO_EXT.contains(ext)) return MediaKind.VIDEO;
        throw new IllegalArgumentException("지원하지 않는 확장자입니다: " + extension);
    }
}