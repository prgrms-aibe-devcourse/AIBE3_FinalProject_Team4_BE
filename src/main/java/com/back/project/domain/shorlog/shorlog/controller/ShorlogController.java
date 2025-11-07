package com.back.project.domain.shorlog.shorlog.controller;

import com.back.project.domain.shorlog.shorlog.dto.CreateShorlogRequest;
import com.back.project.domain.shorlog.shorlog.dto.CreateShorlogResponse;
import com.back.project.domain.shorlog.shorlog.service.ShorlogService;
import com.back.project.global.rsData.RsData;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/shorlog")
@RequiredArgsConstructor
public class ShorlogController {

    private final ShorlogService shorlogService;

    @PostMapping
    public ResponseEntity<RsData<CreateShorlogResponse>> createShorlog(
            @RequestAttribute("userId") Long userId,
            @Valid @RequestBody CreateShorlogRequest request
    ) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(RsData.successOf(shorlogService.createShorlog(userId, request)));
    }
}