package com.back.domain.blog.blogFile.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.back.domain.blog.blog.entity.Blog;
import com.back.domain.blog.blog.repository.BlogRepository;
import com.back.domain.blog.blogFile.dto.BlogMediaUploadResponse;
import com.back.domain.blog.blogFile.entity.BlogFile;
import com.back.domain.blog.blogFile.entity.MediaKind;
import com.back.domain.blog.blogFile.repository.BlogFileRepository;
import com.back.domain.blog.blogFile.util.ImageResizeUtil;
import com.back.domain.blog.blogFile.util.MediaTypeDetector;
import com.back.domain.blog.blogFile.util.VideoResizeUtil;
import com.back.domain.shared.image.entity.Image;
import com.back.domain.shared.image.entity.ImageType;
import com.back.domain.shared.image.repository.ImageRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BlogMediaService {

    private static final long MAX_IMAGE_SIZE = 5L * 1024 * 1024;       // 5MB
    private static final long MAX_VIDEO_SIZE = 100L * 1024 * 1024;     // 100MB
    private static final String BLOG_FOLDER = "blogs/";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp", "mp4", "mov", "avi"};
    private final AmazonS3 amazonS3;
    private final ImageRepository imageRepository;
    private final BlogFileRepository blogFileRepository;
    private final BlogRepository blogRepository;
    private final UserRepository userRepository;
    private final MediaTypeDetector mediaTypeDetector;
    private final ImageResizeUtil imageResizeUtil;
    private final VideoResizeUtil videoResizeUtil;
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public BlogMediaUploadResponse uploadBlogMedia(Long userId, Long blogId, MultipartFile file, ImageType type, String aspectRatios) {
        validateFile(file);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        Blog blog = blogRepository.findById(blogId)
                .orElseThrow(() -> new IllegalArgumentException("블로그를 찾을 수 없습니다."));

        String originalFilename = Optional.ofNullable(file.getOriginalFilename())
                .orElse("file");

        String ext = getExtension(originalFilename);
        MediaKind mediaKind = mediaTypeDetector.detectKind(ext);

        long maxSize = mediaKind == MediaKind.IMAGE ? MAX_IMAGE_SIZE : MAX_VIDEO_SIZE;
        if (file.getSize() > maxSize) {
            throw new IllegalArgumentException(mediaKind == MediaKind.IMAGE
                    ? "이미지 용량은 5MB를 초과할 수 없습니다."
                    : "동영상 용량은 100MB를 초과할 수 없습니다.");
        }

        // 리사이징 처리
        byte[] uploadBytes;
        try {
            if (mediaKind == MediaKind.IMAGE) {
                uploadBytes = imageResizeUtil.resize(file, aspectRatios);
            } else {
                uploadBytes = videoResizeUtil.resizeIfNeeded(file); // 현재는 원본 그대로
            }
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

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(uploadBytes.length);
        metadata.setContentType(file.getContentType());

        try (ByteArrayInputStream bais = new ByteArrayInputStream(uploadBytes)) {
            PutObjectRequest request = new PutObjectRequest(bucket, s3Key, bais, metadata);
            amazonS3.putObject(request);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }

        String s3Url = amazonS3.getUrl(bucket, s3Key).toString();

        int sortOrder = blogFileRepository.findByBlog(blog).size();

        Image image = Image.create(
                user,
                type,
                originalFilename,
                savedFilename,
                s3Url,
                uploadBytes.length,
                file.getContentType()
        );

        Image savedImage = imageRepository.save(image);

        BlogFile blogFile = BlogFile.create(blog, savedImage, sortOrder);
        blogFileRepository.save(blogFile);

        if (type == ImageType.THUMBNAIL) {
            blog.changeThumbnailUrl(s3Url);
        }

        return new BlogMediaUploadResponse(savedImage, mediaKind);
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
            throw new IllegalArgumentException("파일 크기는 5MB를 초과할 수 없습니다.");
        }
        String ext = getExtension(file.getOriginalFilename());
        if (!List.of(ALLOWED_EXTENSIONS).contains(ext.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (JPG, JPEG, PNG, WEBP)");
        }
    }

}
