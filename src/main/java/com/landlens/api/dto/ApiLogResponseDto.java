package com.landlens.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class ApiLogResponseDto {
    private UUID id;
    private UUID apiKeyId;
    private String endpoint;
    private String method;
    private Integer statusCode;
    private String ipAddress;
    private Instant requestTimestamp;
    private Integer responseTimeMs;
}
