package com.back.domain.user.user.file;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@Component
public class S3Uploader {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.region.static}")
    private String region;

    public String upload(MultipartFile file, String dirName) throws IOException {

        String ext = getExtension(file.getOriginalFilename());
        String fileName = dirName + "/" + UUID.randomUUID() + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(
                new PutObjectRequest(bucket, fileName, file.getInputStream(), metadata)
        );

        return generateUrl(fileName);
    }

    public void delete(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) return;

        String key = imageUrl.replace(generateBaseUrl() + "/", "");
        amazonS3.deleteObject(bucket, key);
    }

    private String generateUrl(String key) {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com/" + key;
    }

    private String generateBaseUrl() {
        return "https://" + bucket + ".s3." + region + ".amazonaws.com";
    }

    private String getExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }
}
