package com.projects.distributed_lovable.intelligence_service.dto.chat;


import com.projects.distributed_lovable.common_lib.enums.MessageRole;

import java.time.Instant;
import java.util.List;

public record ChatResponse(

        Long id,
        List<ChatEventResponse> events,
        String content,
        MessageRole role,
        Integer tokensUsed,
        Instant createdAt
) {
}
