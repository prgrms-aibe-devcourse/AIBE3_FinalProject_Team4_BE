package com.back.domain.shorlog.shorlogtts.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.back.domain.shorlog.shorlog.entity.Shorlog;
import com.back.domain.shorlog.shorlog.repository.ShorlogRepository;
import com.back.domain.shorlog.shorlogtts.dto.TtsResponse;
import com.back.domain.shorlog.shorlogtts.entity.ShorlogTtsUsage;
import com.back.domain.shorlog.shorlogtts.exception.TtsErrorCase;
import com.back.domain.shorlog.shorlogtts.repository.ShorlogTtsUsageRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
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
    private final ShorlogTtsUsageRepository ttsUsageRepository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final String S3_TTS_FOLDER = "shorlog/tts/";
    private static final int CHARS_PER_TOKEN = 400;

    public TtsResponse getTtsUrl(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new ServiceException(TtsErrorCase.SHORLOG_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(TtsErrorCase.USER_NOT_FOUND));

        if (shorlog.getTtsUrl() == null) {
            return TtsResponse.of(null, user.getTtsToken());
        }

        return TtsResponse.of(shorlog.getTtsUrl(), user.getTtsToken());
    }

    @Transactional
    public TtsResponse generateTts(Long shorlogId, Long userId) {
        Shorlog shorlog = shorlogRepository.findById(shorlogId)
                .orElseThrow(() -> new ServiceException(TtsErrorCase.SHORLOG_NOT_FOUND));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ServiceException(TtsErrorCase.USER_NOT_FOUND));

        // 이미 TTS가 생성된 경우
        if (shorlog.getTtsUrl() != null) {
            // 이미 이 사용자가 토큰을 지불한 적이 있는지 확인
            boolean hasUsed = ttsUsageRepository.existsByShorlogIdAndUserId(shorlogId, userId);

            if (hasUsed) {
                // 이미 사용한 적이 있으면 토큰 차감 없이 반환
                log.info("[TTS] 사용자 {}가 이미 숏로그 {}의 TTS를 사용한 기록이 있음 - 토큰 차감 없음", userId, shorlogId);
                return TtsResponse.of(shorlog.getTtsUrl(), user.getTtsToken());
            } else {
                // 처음 사용하는 경우 토큰 차감
                int contentLength = shorlog.getContent().length();
                int requiredTokens = (int) Math.ceil((double) contentLength / CHARS_PER_TOKEN);

                if (user.getTtsToken() < requiredTokens) {
                    throw new ServiceException(TtsErrorCase.TTS_TOKEN_INSUFFICIENT);
                }

                user.useTtsToken(requiredTokens);

                // 사용 기록 저장
                ShorlogTtsUsage usage = ShorlogTtsUsage.create(shorlogId, userId);
                ttsUsageRepository.save(usage);

                log.info("[TTS] 사용자 {}가 숏로그 {}의 TTS 처음 사용 - 토큰 {} 차감", userId, shorlogId, requiredTokens);
                return TtsResponse.of(shorlog.getTtsUrl(), user.getTtsToken());
            }
        }

        // TTS가 아직 생성되지 않은 경우
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

            // Shorlog 엔티티에 TTS URL과 생성자 ID 저장
            shorlog.updateTtsUrl(s3Url);
            shorlog.updateTtsCreatorId(userId);

            // 사용 기록 저장
            ShorlogTtsUsage usage = ShorlogTtsUsage.create(shorlogId, userId);
            ttsUsageRepository.save(usage);

            log.info("[TTS] 사용자 {}가 숏로그 {}의 TTS 생성 - 토큰 {} 차감", userId, shorlogId, requiredTokens);
            return TtsResponse.of(s3Url, user.getTtsToken());

        } catch (IOException e) {
            log.error("TTS 생성 실패: {}", e.getMessage(), e);
            throw new ServiceException(TtsErrorCase.TTS_GENERATION_FAILED, e);
        }
    }

    private byte[] synthesizeSpeech(String text) throws IOException {
        String credentialsPath = System.getProperty("GOOGLE_APPLICATION_CREDENTIALS");

        if (credentialsPath == null || credentialsPath.isEmpty()) {
            throw new IOException("Google Cloud 인증 정보가 설정되지 않았습니다.");
        }

        try {
            GoogleCredentials credentials = ServiceAccountCredentials.fromStream(
                new java.io.FileInputStream(credentialsPath)
            ).createScoped("https://www.googleapis.com/auth/cloud-platform");

            TextToSpeechSettings settings = TextToSpeechSettings.newBuilder()
                .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                .build();

            try (TextToSpeechClient textToSpeechClient = TextToSpeechClient.create(settings)) {

                SynthesisInput input = SynthesisInput.newBuilder()
                        .setText(text)
                        .build();

                VoiceSelectionParams voice = VoiceSelectionParams.newBuilder()
                        .setLanguageCode("ko-KR")
                        .setName("ko-KR-Standard-A")
                        .setSsmlGender(SsmlVoiceGender.FEMALE)
                        .build();

                AudioConfig audioConfig = AudioConfig.newBuilder()
                        .setAudioEncoding(AudioEncoding.MP3)
                        .setSpeakingRate(1.0)
                        .setPitch(0.0)
                        .build();

                SynthesizeSpeechResponse response = textToSpeechClient.synthesizeSpeech(
                        input, voice, audioConfig
                );

                ByteString audioContents = response.getAudioContent();
                return audioContents.toByteArray();
            }
        } catch (Exception e) {
            log.error("Google Cloud TTS API 호출 실패: {}", e.getMessage());
            throw new IOException("TTS 음성 합성 실패: " + e.getMessage(), e);
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


        return s3Url;
    }
}

