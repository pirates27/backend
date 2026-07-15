package com.landlens.ai.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class AiVerificationResponseDto {
    private UUID id;
    private UUID propertyId;
    private BigDecimal aiTrustScore;
    private BigDecimal forgeryScore;
    private BigDecimal duplicateScore;
    private Boolean ownershipMatch;
    private BigDecimal riskScore;
    private String summary;
    private BigDecimal confidence;
    private Instant generatedDate;
}
