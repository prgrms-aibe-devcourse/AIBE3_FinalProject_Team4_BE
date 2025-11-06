package com.back;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class Aibe3FinalProjectTeam4Application {

    public static void main(String[] args) {
        SpringApplication.run(Aibe3FinalProjectTeam4Application.class, args);
    }

}
