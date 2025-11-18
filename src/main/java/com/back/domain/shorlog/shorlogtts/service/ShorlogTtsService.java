package com.back.domain.shorlog.shorlogtts.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogtts.exception.TtsErrorCase;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.google.cloud.texttospeech.v1.*;
import com.google.protobuf.ByteString;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ShorlogTtsService {

    private final ShorlogRepository shorlogRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final String S3_TTS_FOLDER = "shorlog/tts/";
    private static final int CHARS_PER_TOKEN = 400;  // 1토큰 = 400자

    @Transactional
    public String generateTts(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new ServiceException(TtsErrorCase.SHORLOG_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(TtsErrorCase.USER_NOT_FOUND));

        if (shorlog.getTtsUrl() != null) {
            log.info("캐시된 TTS 반환: {}", shorlog.getTtsUrl());
            return shorlog.getTtsUrl();
        }
        int contentLength = shorlog.getContent().length();
        int requiredTokens = (int) Math.ceil((double) contentLength / CHARS_PER_TOKEN);

        // 토큰 부족 시 예외 발생 (프론트에서 Web Speech API 사용)
        if (user.getTtsToken() < requiredTokens) {
            throw new ServiceException(TtsErrorCase.TTS_TOKEN_INSUFFICIENT);
        }

        user.useTtsToken(requiredTokens);

        try {
            byte[] audioBytes = synthesizeSpeech(shorlog.getContent());

            // S3에 업로드
            String s3Url = uploadToS3(audioBytes, shorlogId);

            // Shorlog 엔티티에 TTS URL 저장
            shorlog.updateTtsUrl(s3Url);

            log.info("TTS 생성 완료 - 숏로그 ID: {}, 사용 토큰: {}, URL: {}",
                    shorlogId, requiredTokens, s3Url);

            return s3Url;

        } catch (IOException e) {
            log.error("TTS 생성 실패: {}", e.getMessage(), e);
            throw new ServiceException(TtsErrorCase.TTS_GENERATION_FAILED, e);
        }
    }

     // Google Cloud Text-to-Speech API 호출
    private byte[] synthesizeSpeech(String text) throws IOException {
        try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create()) {
            SynthesisInput input = SynthesisInput.newBuilder()
                    .setText(text)
                    .build();

            // 음성 설정
            VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                    .setLanguageCode("ko-KR")
                    .setName("ko-KR-Standard-B")  // Standard 음성 (무료)
                    .setSsmlGender(SsmlVoiceGender.FEMALE)
                    .build();

            AudioConfig audioConfig = AudioConfig.newBuilder()
                    .setAudioEncoding(AudioEncoding.MP3)
                    .setSpeakingRate(1.0)  // 속도 (1.0 = 기본)
                    .setPitch(0.0)  // 음높이 (0.0 = 기본)
                    .build();

            SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(
                    input, voice, audioConfig
            );

            ByteString audioContents = response.getAudioContent();
            return audioContents.toByteArray();
        }
    }

    private String uploadToS3(byte[] audioBytes, Long shorlogId) {
        String filename = "shorlog-" + shorlogId + "-" + UUID.randomUUID() + ".mp3";
        String s3Key = S3_TTS_FOLDER + filename;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(audioBytes.length);
        metadata.setContentType("audio/mpeg");

        ByteArrayInputStream inputStream = new ByteArrayInputStream(audioBytes);

        PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, s3Key, inputStream, metadata);

        amazonS3.putObject(putObjectRequest);

        String s3Url = amazonS3.getUrl(bucket, s3Key).toString();

        log.info("S3 TTS 업로드 성공: {}", s3Url);

        return s3Url;
    }

    public String getTtsUrl(Long shorlogId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new ServiceException(TtsErrorCase.SHORLOG_NOT_FOUND));

        if (shorlog.getTtsUrl() == null) {
            throw new ServiceException(TtsErrorCase.TTS_NOT_FOUND);
        }

        return shorlog.getTtsUrl();
    }
}

