package com.landlens.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class AiConversationResponseDto {
    private UUID id;
    private String title;
    private UUID userId;
    private Instant createdAt;
}
