package com.projects.distributed_lovable.intelligence_service.service;


import com.projects.distributed_lovable.intelligence_service.dto.chat.StreamResponse;
import reactor.core.publisher.Flux;

public interface AiGenerationService {
    Flux<StreamResponse> streamResponseFromLLM(String message, Long projectId);
}
