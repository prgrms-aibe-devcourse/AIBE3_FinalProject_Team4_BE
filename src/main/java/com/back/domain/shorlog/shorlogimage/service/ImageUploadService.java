package com.back.domain.shorlog.shorlogimage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.back.domain.image.image.util.ImageUrlToMultipartFile;
import com.back.domain.shared.image.entity.Image;
import com.back.domain.shared.image.entity.ImageType;
import com.back.domain.shared.image.repository.ImageRepository;
import com.back.domain.shorlog.shorlogimage.dto.ImageOrderItemType;
import com.back.domain.shorlog.shorlogimage.dto.UploadImageOrderRequest;
import com.back.domain.shorlog.shorlogimage.dto.UploadImageResponse;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ImageUploadService {

    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final ImageUrlToMultipartFile imageUrlToMultipartFile;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_EXTENSIONS = {"jpg", "jpeg", "png", "webp"};
    private static final int MAX_WIDTH = 1080;
    private static final int MAX_HEIGHT = 1350;
    private static final String S3_FOLDER = "shorlog/images/";

    @Transactional
    public List<UploadImageResponse> uploadImages(Long userId, List<MultipartFile> files, List<UploadImageOrderRequest> orderItems) {
        if (orderItems == null || orderItems.isEmpty()) {
            throw new IllegalArgumentException("파일은 최소 1개 이상 필요합니다.");
        }

        if (orderItems.size() > 10) {
            throw new IllegalArgumentException("파일은 최대 10개까지 업로드 가능합니다.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        List<UploadImageOrderRequest> sortedItems = orderItems.stream()
                .sorted(Comparator.comparingInt(UploadImageOrderRequest::order))
                .toList();

        List<MultipartFile> finalFiles = (files == null) ? List.of() : files;

        return sortedItems.stream()
                .parallel()
                .map(item -> processAndUploadSingleImage(item, finalFiles, user))
                .toList();
    }

    private UploadImageResponse processAndUploadSingleImage(
            UploadImageOrderRequest item,
            List<MultipartFile> files,
            User user) {

        try {
            MultipartFile file;
            if (item.type() == ImageOrderItemType.URL) {
                file = imageUrlToMultipartFile.convert(item.url(), "files");
            } else {
                file = (item.fileIndex() < files.size()) ? files.get(item.fileIndex()) : null;
            }

            String aspectRatio = (item.aspectRatio() != null && !item.aspectRatio().isBlank())
                    ? item.aspectRatio()
                    : "original";

            validateFile(file);

            String originalFilename = file.getOriginalFilename();
            String extension = getFileExtension(originalFilename);
            String savedFilename = generateUniqueFilename(extension);
            String s3Key = S3_FOLDER + savedFilename;

            BufferedImage originalImage = ImageIO.read(file.getInputStream());
            BufferedImage resizedImage = resizeImageByAspectRatio(originalImage, aspectRatio);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(resizedImage, extension.equals("jpg") ? "jpeg" : extension, baos);
            byte[] imageBytes = baos.toByteArray();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType(file.getContentType());

            ByteArrayInputStream inputStream = new ByteArrayInputStream(imageBytes);
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, s3Key, inputStream, metadata);

            amazonS3.putObject(putObjectRequest);

            String s3Url = amazonS3.getUrl(bucket, s3Key).toString();

            Image image = Image.create(
                    user,
                    ImageType.THUMBNAIL,
                    originalFilename,
                    savedFilename,
                    s3Url,
                    imageBytes.length,
                    file.getContentType()
            );

            Image savedImage = imageRepository.save(image);


            return UploadImageResponse.from(savedImage);

        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + item, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        String extension = getFileExtension(file.getOriginalFilename());
        if (!List.of(ALLOWED_EXTENSIONS).contains(extension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (JPG, PNG, WEBP만 가능)");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("잘못된 파일명입니다.");
        }
        return filename.substring(filename.lastIndexOf(".") + 1).toLowerCase();
    }

    private String generateUniqueFilename(String extension) {
        return UUID.randomUUID() + "." + extension;
    }

    private BufferedImage resizeImageByAspectRatio(BufferedImage originalImage, String aspectRatio) {
        if (aspectRatio == null || aspectRatio.equalsIgnoreCase("original")) {
            return resizeKeepingRatio(originalImage);
        }

        return switch (aspectRatio.toLowerCase()) {
            case "1:1" -> resizeToSquare(originalImage);
            case "4:5" -> resizeToCrop(originalImage, 1600, 2000);
            case "16:9" -> resizeToCrop(originalImage, MAX_WIDTH, 1125);
            default -> resizeKeepingRatio(originalImage);
        };
    }

    private BufferedImage resizeKeepingRatio(BufferedImage originalImage) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        if (originalWidth <= MAX_WIDTH && originalHeight <= MAX_HEIGHT) {
            return originalImage;
        }

        double widthRatio = (double) MAX_WIDTH / originalWidth;
        double heightRatio = (double) MAX_HEIGHT / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        return createResizedImage(originalImage, newWidth, newHeight);
    }

    private BufferedImage resizeToSquare(BufferedImage originalImage) {
        return resizeToCrop(originalImage, MAX_WIDTH, MAX_WIDTH);
    }

    private BufferedImage resizeToCrop(BufferedImage originalImage, int targetWidth, int targetHeight) {
        int originalWidth = originalImage.getWidth();
        int originalHeight = originalImage.getHeight();

        double targetRatio = (double) targetWidth / targetHeight;
        double originalRatio = (double) originalWidth / originalHeight;

        int cropX = 0, cropY = 0, cropWidth = originalWidth, cropHeight = originalHeight;

        if (originalRatio > targetRatio) {
            cropWidth = (int) (originalHeight * targetRatio);
            cropX = (originalWidth - cropWidth) / 2;
        } else if (originalRatio < targetRatio) {
            cropHeight = (int) (originalWidth / targetRatio);
            cropY = (originalHeight - cropHeight) / 2;
        }

        BufferedImage croppedImage = originalImage.getSubimage(cropX, cropY, cropWidth, cropHeight);

        return createResizedImage(croppedImage, targetWidth, targetHeight);
    }

    private BufferedImage createResizedImage(BufferedImage originalImage, int targetWidth, int targetHeight) {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resizedImage.createGraphics();

        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        graphics.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();

        return resizedImage;
    }
}

