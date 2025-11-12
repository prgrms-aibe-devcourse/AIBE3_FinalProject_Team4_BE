package com.back;

import com.back.domain.user.mail.service.MailService;
import com.back.domain.user.mail.service.VerificationTokenService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class Aibe3FinalProjectTeam4ApplicationTests {

    @TestConfiguration
    static class TestBeanConfig {
        @Bean
        public JavaMailSender javaMailSender() {
            return Mockito.mock(JavaMailSender.class);
        }

        @Bean
        public RedisTemplate<?, ?> redisTemplate() {
            return Mockito.mock(RedisTemplate.class);
        }

        @Bean
        public MailService mailService() {
            return Mockito.mock(MailService.class);
        }

        @Bean
        public VerificationTokenService verificationTokenService() {
            return Mockito.mock(VerificationTokenService.class);
        }
    }

    @Test
    void contextLoads() {
    }

}
