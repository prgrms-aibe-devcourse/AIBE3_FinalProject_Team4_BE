package com.back.domain.blog.blogFile.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.back.domain.blog.blog.dto.BlogIndexEvent;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.exception.BlogErrorCase;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogFile.dto.BlogMediaUploadResponse;
import com.back.domain.blog.blogFile.entity.BlogFile;
import com.back.domain.blog.blogFile.entity.MediaKind;
import com.back.domain.blog.blogFile.repository.BlogFileRepository;
import com.back.domain.blog.blogFile.util.ImageResizeUtil;
import com.back.domain.blog.blogFile.util.MediaTypeDetector;
import com.back.domain.blog.blogFile.util.VideoResizeUtil;
import com.back.domain.image.image.util.ImageUrlToMultipartFile;
import com.back.domain.shared.image.entity.Image;
import com.back.domain.shared.image.entity.ImageType;
import com.back.domain.shared.image.repository.ImageRepository;
import com.back.domain.shared.image.service.ImageLifecycleService;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogMediaService {
    private static final long MAX_IMAGE_SIZE = 10L * 1024 * 1024;      // 10MB
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;     // 100MB
    private static final String BLOG_FOLDER = "blogs/";
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;        // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp", "mp4", "mov", "avi"};
    private final AmazonS3 amazonS3;

    private final ImageRepository imageRepository;
    private final BlogFileRepository blogFileRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final ImageLifecycleService imageLifecycleService;
    private final ApplicationEventPublisher eventPublisher;
    // Utils
    private final MediaTypeDetector mediaTypeDetector;
    private final ImageResizeUtil imageResizeUtil;
    private final VideoResizeUtil videoResizeUtil;
    private final ImageUrlToMultipartFile imageUrlToMultipartFile;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public BlogMediaUploadResponse uploadBlogMedia(Long userId, Long blogId, MultipartFile file, String apiImageUrl, ImageType type, String aspectRatios) {
        // 무료 이미지 API에서 가져온 경우 URL을 파일로 변환하기
        MultipartFile finalFile = (apiImageUrl != null && !apiImageUrl.isBlank() && (file == null || file.isEmpty()))
                ? imageUrlToMultipartFile.convert(apiImageUrl, "files")
                : file;
        validateFile(finalFile);

        // 1. 리사이즈 & 메타 정보 계산 (트랜잭션 X)
        String originalFilename = Optional.ofNullable(finalFile.getOriginalFilename()).orElse("file");
        String ext = getExtension(originalFilename);
        MediaKind mediaKind = mediaTypeDetector.detectKind(ext);

        long maxSize = mediaKind == MediaKind.IMAGE ? MAX_IMAGE_SIZE : MAX_VIDEO_SIZE;
        if (finalFile.getSize() > maxSize) {
            throw new IllegalArgumentException(
                    mediaKind == MediaKind.IMAGE
                            ? "이미지 용량은 10MB를 초과할 수 없습니다."
                            : "동영상 용량은 100MB를 초과할 수 없습니다."
            );
        }

        byte[] uploadBytes;
        try {
            uploadBytes = (mediaKind == MediaKind.IMAGE)
                    ? imageResizeUtil.resize(finalFile, aspectRatios)
                    : videoResizeUtil.resizeIfNeeded(finalFile);
        } catch (IOException e) {
            throw new RuntimeException("파일 리사이징 중 오류가 발생했습니다.", e);
        }

        String savedFilename = UUID.randomUUID() + "." + ext;

        String folder = BLOG_FOLDER + blogId + "/";
        if (type == ImageType.THUMBNAIL) {
            folder += "thumbnail/";
        } else {
            folder += (mediaKind == MediaKind.VIDEO ? "video/" : "content/");
        }
        String s3Key = folder + savedFilename;

        // 2. S3 업로드 (트랜잭션 X)
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(uploadBytes.length);
        metadata.setContentType(finalFile.getContentType());

        try (ByteArrayInputStream bais = new ByteArrayInputStream(uploadBytes)) {
            PutObjectRequest request = new PutObjectRequest(bucket, s3Key, bais, metadata);
            amazonS3.putObject(request);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }

        String s3Url = amazonS3.getUrl(bucket, s3Key).toString();

        // 3. DB 저장 (짧은 트랜잭션)
        try {
            return saveImageMetadata(userId, blogId, type, mediaKind, originalFilename, savedFilename, s3Url, uploadBytes.length, finalFile.getContentType()
            );
        } catch (RuntimeException e) {
            // DB 쪽에서 실패하면 S3 정리 (베스트 에포트)
            safeDeleteFromS3(s3Key);
            throw e;
        }
    }
    
    protected BlogMediaUploadResponse saveImageMetadata(Long userId, Long blogId, ImageType type, MediaKind mediaKind, String originalFilename, String savedFilename, String s3Url, long size, String contentType
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("블로그를 찾을 수 없습니다."));

        Image image = Image.create(user, type, originalFilename, savedFilename, s3Url, size, contentType);

        Image savedImage = imageRepository.save(image);

        if (type == ImageType.THUMBNAIL) {
            blog.changeThumbnailUrl(s3Url);
            eventPublisher.publishEvent(new BlogIndexEvent(blogId));
            return new BlogMediaUploadResponse(savedImage, mediaKind);
        }

        int sortOrder = blogFileRepository.findByBlog(blog).size();
        BlogFile blogFile = BlogFile.create(blog, savedImage, sortOrder);
        blogFileRepository.save(blogFile);

        return new BlogMediaUploadResponse(savedImage, mediaKind);
    }

    private void safeDeleteFromS3(String key) {
        try {
            amazonS3.deleteObject(bucket, key);
        } catch (Exception e) {
            log.warn("S3 삭제 실패 key={}", key, e);
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("잘못된 파일명입니다.");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!List.of(ALLOWED_EXTENSIONS).contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (JPG, JPEG, PNG, WEBP)");
        }
    }

    @Transactional
    public void deleteBlogMedia(Long userId, Long blogId, Long imageId) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        BlogFile target = blogFileRepository.findByBlog_IdAndImage_Id(blogId, imageId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.FILE_NOT_FOUND));

        int deletedOrder = target.getSortOrder();
        blogFileRepository.delete(target);
        List<BlogFile> afterFiles =
                blogFileRepository.findAllByBlog_IdAndSortOrderGreaterThanOrderBySortOrderAsc(blogId, deletedOrder);
        for (BlogFile bf : afterFiles) {
            bf.updateSortOrder(bf.getSortOrder() - 1);
        }
        blogFileRepository.flush();
        imageLifecycleService.decrementReference(target.getImage().getS3Url());
    }

    @Transactional
    public void reorderBlogFiles(Long userId, Long blogId, List<Long> imageIds) {
        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new ServiceException(BlogErrorCase.BLOG_NOT_FOUND));
        if (!blog.getUser().getId().equals(userId)) {
            throw new ServiceException(BlogErrorCase.PERMISSION_DENIED);
        }
        List<BlogFile> files = blogFileRepository.findAllByBlog_IdOrderBySortOrderAsc(blogId);
        if (files.size() != imageIds.size()) {
            throw new ServiceException(BlogErrorCase.FILE_COUNT_MISMATCH);
        }
        Map<Long, BlogFile> fileMap = files.stream()
                .collect(Collectors.toMap(
                        bf -> bf.getImage().getId(),
                        bf -> bf));

        for (Long imageId : imageIds) {
            if (!fileMap.containsKey(imageId)) {
                throw new ServiceException(BlogErrorCase.FILE_NOT_FOUND);
            }
        }
        int order = 0;
        for (Long imageId : imageIds) {
            BlogFile bf = fileMap.get(imageId);
            bf.updateSortOrder(order++);
        }
        blogFileRepository.flush();
        eventPublisher.publishEvent(new BlogIndexEvent(blogId));
    }
}
