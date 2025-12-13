package com.back.domain.ai.model.service;

import com.back.domain.ai.model.dto.ModelAvailabilityDto;
import com.back.domain.ai.model.exception.ModelUsageExceededException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelUsageService {

    private final ModelUsageTxService modelUsageTxService;

    public List<ModelAvailabilityDto> getAllModelAvailabilities(Long userId) {
        return modelUsageTxService.getAllModelAvailabilities(userId);
    }

    // (리액티브) 사용 가능 체크 - JPA 블로킹을 boundedElastic로
    public Mono<Void> checkModelAvailability(Long userId, String modelName) {
        return Mono.fromCallable(() -> modelUsageTxService.getModelAvailability(userId, modelName))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(availabilityDto -> {
                    if (!availabilityDto.available()) {
                        return Mono.error(new ModelUsageExceededException("MODEL_USAGE_LIMIT_EXCEEDED"));
                    }
                    return Mono.empty();
                });
    }

    // (리액티브) 사용 횟수 증가 - JPA 블로킹을 boundedElastic로
    public Mono<ModelAvailabilityDto> increaseCountAsync(Long userId, String model) {
        return Mono.fromCallable(() -> modelUsageTxService.increaseCount(userId, model))
                .subscribeOn(Schedulers.boundedElastic());
    }
}