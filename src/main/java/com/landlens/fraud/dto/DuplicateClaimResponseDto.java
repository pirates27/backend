package com.landlens.fraud.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class DuplicateClaimResponseDto {
    private UUID id;
    private UUID propertyAId;
    private UUID propertyBId;
    private BigDecimal similarity;
    private String reason;
    private String status;
    private String decision;
    private Boolean isActive;
    private Instant createdAt;
}
