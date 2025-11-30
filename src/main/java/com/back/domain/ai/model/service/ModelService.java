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

        modelRepository.save(new Model("gpt-4o-mini", 6));
        modelRepository.save(new Model("gpt-4.1-mini", 2));
        modelRepository.save(new Model("gpt-5-mini", 2));
    }
}
