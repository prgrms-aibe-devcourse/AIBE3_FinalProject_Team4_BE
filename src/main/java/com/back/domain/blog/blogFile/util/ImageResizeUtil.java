package com.back.domain.blog.blogFile.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class ImageResizeUtil {

    private static final int MAX_WIDTH = 1080;
    private static final int MAX_HEIGHT = 1350;


    //     aspectRatios ("1:1", "4:5", "16:9", "original")에 따라 크롭 또는 리사이징
    public byte[] resize(MultipartFile file, String aspectRatios) throws IOException {
        BufferedImage original = ImageIO.read(file.getInputStream());
        if (original == null) {
            throw new IllegalArgumentException("이미지 파일이 아닙니다.");
        }

        String ratio = (aspectRatios == null || aspectRatios.isBlank())
                ? "original"
                : aspectRatios.toLowerCase();

        BufferedImage processed;

        switch (ratio) {
            case "1:1" -> processed = resizeToCrop(original, MAX_WIDTH, MAX_WIDTH);
            case "4:5" -> processed = resizeToCrop(original, MAX_WIDTH, (int) (MAX_WIDTH * 5.0 / 4.0)); // 1080x1350
            case "16:9" -> processed = resizeToCrop(original, MAX_WIDTH, (int) (MAX_WIDTH * 9.0 / 16.0)); // 1080x608
            case "original" -> processed = resizeKeepingRatio(original);
            default -> processed = resizeKeepingRatio(original);
        }

        String ext = getExtension(file.getOriginalFilename());
        String formatName = ext.equals("jpg") ? "jpeg" : ext;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(processed, formatName, baos);
        return baos.toByteArray();
    }

    //       내부 유틸 메서드들
    private BufferedImage resizeKeepingRatio(BufferedImage original) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        if (originalWidth <= MAX_WIDTH && originalHeight <= MAX_HEIGHT) {
            return original;
        }

        double widthRatio = (double) MAX_WIDTH / originalWidth;
        double heightRatio = (double) MAX_HEIGHT / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);

        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);

        return createResizedImage(original, newWidth, newHeight);
    }

    private BufferedImage resizeToCrop(BufferedImage original, int targetWidth, int targetHeight) {
        int originalWidth = original.getWidth();
        int originalHeight = original.getHeight();

        double targetRatio = (double) targetWidth / targetHeight;
        double originalRatio = (double) originalWidth / originalHeight;

        int cropX = 0, cropY = 0;
        int cropWidth = originalWidth;
        int cropHeight = originalHeight;

        if (originalRatio > targetRatio) {
            cropWidth = (int) (originalHeight * targetRatio);
            cropX = (originalWidth - cropWidth) / 2;
        } else if (originalRatio < targetRatio) {
            cropHeight = (int) (originalWidth / targetRatio);
            cropY = (originalHeight - cropHeight) / 2;
        }

        BufferedImage cropped = original.getSubimage(cropX, cropY, cropWidth, cropHeight);
        return createResizedImage(cropped, targetWidth, targetHeight);
    }

    private BufferedImage createResizedImage(BufferedImage original, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = resized.createGraphics();

        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.drawImage(original, 0, 0, width, height, null);
        g.dispose();

        return resized;
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("잘못된 파일명입니다.");
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase();
    }
}