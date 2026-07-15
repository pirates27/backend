package com.landlens.fraud.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class DuplicateClaimRequestDto {
    @NotNull(message = "Property A ID is required")
    private UUID propertyAId;

    @NotNull(message = "Property B ID is required")
    private UUID propertyBId;

    @NotNull(message = "Similarity score is required")
    private BigDecimal similarity;

    @NotNull(message = "Reason is required")
    private String reason;

    private String status;

    private String decision;
}
