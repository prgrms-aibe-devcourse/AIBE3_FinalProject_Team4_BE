package com.back.domain.ai.model.service;

import com.back.domain.ai.model.entity.Model;
import com.back.domain.ai.model.repository.ModelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ModelService {

    private final ModelRepository modelRepository;

    public void initModels() {
        if (modelRepository.count() > 0) return;

        modelRepository.save(new Model("gpt-4o-mini", 5));
        modelRepository.save(new Model("gpt-3.5-turbo", 3));
        modelRepository.save(new Model("gpt-4", 2));
    }
}
