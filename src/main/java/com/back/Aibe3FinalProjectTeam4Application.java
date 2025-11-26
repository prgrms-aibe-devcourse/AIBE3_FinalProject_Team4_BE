package com.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
// @Scheduled 활성화를 위한 어노테이션
@EnableScheduling
public class Aibe3FinalProjectTeam4Application {

    public static void main(String[] args) {
        SpringApplication.run(Aibe3FinalProjectTeam4Application.class, args);
    }

}
