package com.landlens.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class AiMessageResponseDto {
    private UUID id;
    private UUID conversationId;
    private String senderRole;
    private String content;
    private Instant timestamp;
}
