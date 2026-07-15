package com.landlens.verification.dto;

import com.landlens.user.dto.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class VerificationTimelineResponseDto {
    private UUID id;
    private UUID propertyId;
    private Instant timestamp;
    private String action;
    private String remarks;
    private UserResponseDto user;
}
