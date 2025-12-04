package com.back.domain.user.user.file;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ProfileImageService {

    private final S3Uploader s3Uploader;

    public String updateFile(String currentImgUrl, MultipartFile newFile, boolean deleteExisting) throws IOException {

        // 새 파일 업로드
        if (newFile != null && !newFile.isEmpty()) {
            if (currentImgUrl != null) s3Uploader.delete(currentImgUrl);
            return s3Uploader.upload(newFile, "profile");
        }

        // 기존 이미지 삭제
        if (deleteExisting && currentImgUrl != null) {
            s3Uploader.delete(currentImgUrl);
            return null;
        }

        // 아무 변경 없음 → 기존 URL 유지
        return currentImgUrl;
    }
}
