package com.landlens.verification.dto;

import com.landlens.user.dto.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class GovernmentVerificationResponseDto {
    private UUID id;
    private UUID propertyId;
    private UserResponseDto officer;
    private String remarks;
    private String status;
    private Instant verifiedDate;
}
