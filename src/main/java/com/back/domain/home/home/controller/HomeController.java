package com.back.domain.home.home.controller;


import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@Slf4j
public class HomeController {

    @GetMapping("/")
    public String home() {
        LocalDateTime localDateTime = LocalDateTime.now();
        log.info("Home " + localDateTime);
        return "Hello, World!";
    }

}