package com.back.domain.ai.model.service;

import com.back.domain.ai.model.dto.ModelAvailabilityDto;
import com.back.domain.ai.model.entity.Model;
import com.back.domain.ai.model.entity.ModelUsage;
import com.back.domain.ai.model.exception.ModelUsageExceededException;
import com.back.domain.ai.model.repository.ModelRepository;
import com.back.domain.ai.model.repository.ModelUsageRepository;
import com.back.domain.user.user.entity.User;
import com.back.domain.user.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ModelUsageService {

    private final ModelRepository modelRepository;
    private final ModelUsageRepository modelUsageRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ModelAvailabilityDto getModelAvailability(Long userId, String modelName) {
        Model model = modelRepository.findByName(modelName)
                .orElseThrow(() -> new IllegalArgumentException("MODEL_NOT_FOUND"));

        ModelUsage modelUsage = modelUsageRepository
                .findByUserIdAndModelId(userId, model.getId())
                .orElse(null);

        return toAvailabilityDto(model, modelUsage);
    }

    @Transactional(readOnly = true)
    public List<ModelAvailabilityDto> getAllModelAvailabilities(Long userId) {

        List<Object[]> rows = modelRepository.findModelsWithUsage(userId);

        return rows.stream().map(row -> toAvailabilityDto(
                (Model) row[0],
                (ModelUsage) row[1]
        )).toList();
    }

    @Transactional
    public ModelAvailabilityDto increaseCount(Long userId, String modelName) {

        Model model = modelRepository.findByName(modelName)
                .orElseThrow(() -> new IllegalArgumentException("MODEL_NOT_FOUND"));

        // 동시성 안전하게 usage row 잠금
        ModelUsage usage = modelUsageRepository.findByUserIdAndModelIdForUpdate(userId, model.getId())
                .orElseGet(() -> createModelUsage(userId, model.getId()));

        if (!isToday(usage.getModifiedAt())) {
            usage.setCount(0);
        }

        int newUsedCount = usage.getCount() + 1;
        usage.setCount(newUsedCount);
        modelUsageRepository.save(usage);

        return new ModelAvailabilityDto(model.getId(), model.getName(), newUsedCount < model.getLimitCount());
    }

    /**
     * (리액티브) 사용 가능 체크 - JPA 블로킹을 boundedElastic로
     */
    public Mono<Void> checkModelAvailability(Long userId, String modelName) {
        return Mono.fromCallable(() -> getModelAvailability(userId, modelName))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(availabilityDto -> {
                    if (!availabilityDto.available()) {
                        return Mono.error(new ModelUsageExceededException("MODEL_USAGE_LIMIT_EXCEEDED"));
                    }
                    return Mono.empty();
                });
    }

    /**
     * (리액티브) 사용 횟수 증가 - JPA 블로킹을 boundedElastic로
     */
    public Mono<ModelAvailabilityDto> increaseCountAsync(Long userId, String model) {
        return Mono.fromCallable(() -> increaseCount(userId, model))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private ModelAvailabilityDto toAvailabilityDto(Model model, ModelUsage modelUsage) {
        int usedCount = 0;
        if (modelUsage != null && isToday(modelUsage.getModifiedAt())) {
            usedCount = modelUsage.getCount();
        }

        boolean availability = usedCount < model.getLimitCount();

        return new ModelAvailabilityDto(model.getId(), model.getName(), availability);
    }

    private ModelUsage createModelUsage(Long userId, Long modelId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("USER_NOT_FOUND"));

        Model model = modelRepository.findById(modelId)
                .orElseThrow(() -> new IllegalArgumentException("MODEL_NOT_FOUND"));

        ModelUsage usage = new ModelUsage(user, model, 0);
        return modelUsageRepository.save(usage);
    }

    private int getTodayUsedCount(ModelUsage usage) {
        if (!isToday(usage.getModifiedAt())) {
            usage.setCount(0);
        }
        return usage.getCount();
    }

    private boolean isToday(LocalDateTime dateTime) {
        return dateTime.toLocalDate().equals(LocalDate.now());
    }
}