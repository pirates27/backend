package com.landlens.fraud.dto;

import com.landlens.user.dto.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class FraudReportResponseDto {
    private UUID id;
    private UserResponseDto reporter;
    private UUID propertyId;
    private String reason;
    private String description;
    private String status;
    private UserResponseDto officer;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
