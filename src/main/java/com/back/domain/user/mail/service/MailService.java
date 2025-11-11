package com.back.domain.user.mail.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MailService {
    private final JavaMailSender javaMailSender;
    private final RedisTemplate<String, String> redisTemplate;

    public String createAuthCode() {
        StringBuilder authCode = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int digit = (int) (Math.random() * 10);
            authCode.append(digit);
        }
        return authCode.toString();
    }

    public void sendMail(String toEmail, String authCode) throws MessagingException {

        MimeMessage mimeMessage = javaMailSender.createMimeMessage();

        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

        helper.setTo(toEmail);
        helper.setSubject("TexTok 이메일 인증 코드");

        String content = "<html><body>" +
                "<h1>TexTok 이메일 인증 코드</h1>" +
                "<p>아래 인증 코드를 입력해주세요:</p>" +
                "<h2 style='color:blue;'>" + authCode + "</h2>" +
                "</body></html>";

        helper.setText(content, true);  // true: HTML 형식 사용

        javaMailSender.send(mimeMessage);

    }

    public void saveAuthCode(String email, String authCode) {
        redisTemplate.opsForValue().set(
                "emailAuthCode:" + email,
                authCode,
                5 * 60, // 5 minutes
                TimeUnit.SECONDS
        );
    }

    public String getAuthCode(String email) {
        return redisTemplate.opsForValue().get("emailAuthCode:" + email);
    }

    public void deleteAuthCode(String email) {
        redisTemplate.delete("emailAuthCode:" + email);
    }
}
