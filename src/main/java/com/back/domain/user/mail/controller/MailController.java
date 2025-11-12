package com.back.domain.user.mail.controller;

import com.back.domain.user.mail.dto.EmailVerificationTokenResponseDto;
import com.back.domain.user.mail.dto.EmailVerifyRequestDto;
import com.back.domain.user.mail.service.MailService;
import com.back.domain.user.mail.service.VerificationTokenService;
import com.back.global.exception.AuthException;
import com.back.global.rsData.RsData;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class MailController {
    private final MailService mailService;
    private final VerificationTokenService verificationTokenService;

    @PostMapping("/send-code")
    @Operation(summary = "이메일 인증 코드 전송")
    public RsData<Void> sendVerificationCode(@RequestBody String email) {
        try {
            String authCode = mailService.createAuthCode(); // 인증 코드 생성
            mailService.saveAuthCode(email, authCode);  // 인증 코드 저장
            mailService.sendMail(email, authCode); // 이메일 전송
            return new RsData<>(
                    "200-1",
                    "이메일로 인증 코드가 전송되었습니다.",
                    null
            );
        } catch (MessagingException e) {
            System.out.println(e.getMessage());
            throw new AuthException("500-1", "이메일 전송에 실패했습니다.");
        }
    }

    @PostMapping("/verify-code")
    @Operation(summary = "이메일 인증 코드 검증")
    public RsData<EmailVerificationTokenResponseDto> verifyCode(@RequestBody EmailVerifyRequestDto dto) {
        String savedCode = mailService.getAuthCode(dto.email());

        if(savedCode == null) {
            throw new AuthException("400-1", "인증 코드가 만료되었습니다.");
        }

        if (savedCode.equals(dto.code())) {
            mailService.deleteAuthCode(dto.email());
            String verifiedToken = verificationTokenService.generateAndStoreToken(dto.email());
            return RsData.of(
                    "200-2",
                    "이메일 인증이 완료되었습니다.",
                    new EmailVerificationTokenResponseDto(verifiedToken)
            );
        } else {
            throw new AuthException("400-2", "인증 코드가 일치하지 않습니다.");
        }
    }
}
