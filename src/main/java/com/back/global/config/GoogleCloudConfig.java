package com.back.global.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
@Configuration
public class GoogleCloudConfig {

    @Value("${google.cloud.credentials.json:}")
    private String googleCloudCredentialsJson;

    @PostConstruct
    public void setupGoogleCloudCredentials() {
        if (googleCloudCredentialsJson != null && !googleCloudCredentialsJson.trim().isEmpty()) {
            try {
                // Base64 디코딩
                byte[] decodedBytes = Base64.getDecoder().decode(googleCloudCredentialsJson.trim());
                String jsonContent = new String(decodedBytes);

                // JSON 기본 검증
                if (!jsonContent.contains("type") || !jsonContent.contains("project_id")) {
                    throw new RuntimeException("유효하지 않은 서비스 계정 JSON 형식입니다.");
                }

                // 임시 디렉토리에 파일 생성
                Path tempDir = Files.createTempDirectory("google-cloud");
                Path credentialsPath = Paths.get(tempDir.toString(), "service-account.json");

                try (FileWriter writer = new FileWriter(credentialsPath.toFile())) {
                    writer.write(jsonContent);
                }

                // 환경 변수 설정
                System.setProperty("GOOGLE_APPLICATION_CREDENTIALS", credentialsPath.toString());

                log.info("Google Cloud 인증 설정 완료");

            } catch (IllegalArgumentException e) {
                log.error("Google Cloud 인증 Base64 디코딩 실패", e);
            } catch (IOException e) {
                log.error("Google Cloud 인증 파일 생성 실패", e);
            } catch (Exception e) {
                log.error("Google Cloud 인증 설정 실패", e);
            }
        } else {
            log.warn("Google Cloud 인증 정보가 환경 변수에서 제공되지 않음");
        }
    }
}
