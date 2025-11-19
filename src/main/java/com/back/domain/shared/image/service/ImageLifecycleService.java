package com.back.domain.shared.image.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.back.domain.shared.image.entity.Image;
import com.back.domain.shared.image.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageLifecycleService {

    private final ImageRepository imageRepository;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public void incrementReference(String imageUrl) {
        Image image = findByUrl(imageUrl);
        imageRepository.incrementReferenceCount(image.getId());
    }

    @Transactional
    public void decrementReference(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        imageRepository.findBySavedFilename(extractFilename(imageUrl))
                .ifPresent(image -> imageRepository.decrementReferenceCount(image.getId()));
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * *")
    public void cleanupUnusedImages() {
        LocalDateTime expiry = LocalDateTime.now().minusDays(7);

        List<Image> unused = imageRepository.findUnusedImages(expiry);

        for (Image img : unused) {
            deleteFromS3(img);
            imageRepository.delete(img);
        }
    }

    //       private helper methods
    private Image findByUrl(String imageUrl) {
        String filename = extractFilename(imageUrl);

        return imageRepository.findBySavedFilename(filename)
                .orElseThrow(() -> new NoSuchElementException("이미지를 찾을 수 없습니다."));
    }

    private void deleteFromS3(Image img) {
        String url = img.getS3Url(); // 혹은 getUrl()

        String key = extractKeyFromS3Url(url);

        try {
            amazonS3.deleteObject(new DeleteObjectRequest(bucket, key));
            log.info("S3 파일 삭제 완료: {}", key);
        } catch (Exception e) {
            log.error("S3 삭제 실패: {}", key, e);
            throw new RuntimeException("S3 삭제 실패: " + key, e);
        }
    }

    private String extractKeyFromS3Url(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            if (path.startsWith("/")) {
                return path.substring(1);// blog/ or shorlog/ 제거
            }
            return path;
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("잘못된 S3 URL입니다: " + url, e);
        }
    }

    private String extractFilename(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            throw new IllegalArgumentException("이미지 URL이 유효하지 않습니다.");
        }
        return imageUrl.substring(imageUrl.lastIndexOf('/') + 1);
    }
}