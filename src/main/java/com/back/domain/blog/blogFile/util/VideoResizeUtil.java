package com.back.domain.blog.blogFile.util;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;

@Component
public class VideoResizeUtil {

    // 출력 사이즈 (720p)
    private static final String TARGET_RESOLUTION = "1280x720";

    public byte[] resizeIfNeeded(MultipartFile file) throws IOException {
        // 1) 임시 파일 생성
        File inputFile = File.createTempFile("input_", getExtension(file.getOriginalFilename()));
        File outputFile = File.createTempFile("output_", ".mp4");

        try {
            // MultipartFile → input temp file 저장
            file.transferTo(inputFile);

            // 2) FFmpeg 리사이즈 명령어 생성
            String command = String.format(
                    "ffmpeg -y -i %s -vf scale=%s -preset fast -movflags +faststart %s",
                    inputFile.getAbsolutePath(),
                    TARGET_RESOLUTION,
                    outputFile.getAbsolutePath()
            );

            Process process = Runtime.getRuntime().exec(command);

            // ffmpeg 실행 로그 읽기
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[ffmpeg] " + line);
                }
            }

            int exitCode = process.waitFor();
            if (exitCode != 0) {
                System.err.println("FFmpeg exited with code: " + exitCode);
                // 실패 시 원본 그대로 반환
                return file.getBytes();
            }

            // 3) outputFile → byte[]
            return Files.readAllBytes(outputFile.toPath());

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return file.getBytes(); // 실패 시 원본 반환
        } finally {
            // 4) 임시 파일 cleanup
            inputFile.delete();
            outputFile.delete();
        }
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".mp4"; // 기본값
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}
