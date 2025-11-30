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

        return getModelAvailability(model, modelUsage);
    }

    private ModelAvailabilityDto getModelAvailability(Model model, ModelUsage modelUsage) {
        int usedCount = 0;
        if (modelUsage != null && isToday(modelUsage.getModifiedAt())) {
            usedCount = modelUsage.getCount();
        }

        boolean availability = usedCount < model.getLimitCount();

        return new ModelAvailabilityDto(model.getId(), model.getName(), availability);
    }

    @Transactional(readOnly = true)
    public List<ModelAvailabilityDto> getAllModelAvailabilities(Long userId) {

        List<Object[]> rows = modelRepository.findModelsWithUsage(userId);

        return rows.stream().map(row -> getModelAvailability(
                (Model) row[0],
                (ModelUsage) row[1]
        )).toList();
    }

    @Transactional(readOnly = true)
    public Mono<Void> checkModelAvailability(Long userId, String modelName) {
        ModelAvailabilityDto availabilityDto = getModelAvailability(userId, modelName);

        if (!availabilityDto.available()) {
            return Mono.error(new ModelUsageExceededException("MODEL_USAGE_LIMIT_EXCEEDED"));
        }

        return Mono.empty();
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