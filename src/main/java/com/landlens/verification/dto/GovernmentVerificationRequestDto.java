package com.landlens.verification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GovernmentVerificationRequestDto {
    @NotBlank(message = "Status is required")
    private String status;

    private String remarks;
}
