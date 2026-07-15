package com.landlens.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ApiKeyResponseDto {
    private UUID id;
    private String name;
    private String prefix;
    private String status;
    private Instant expiryDate;
    private Boolean isActive;
    private Instant createdAt;
}
