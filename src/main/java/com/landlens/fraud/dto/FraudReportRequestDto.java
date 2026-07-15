package com.landlens.fraud.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FraudReportRequestDto {
    @NotBlank(message = "Reason is required")
    private String reason;

    @NotBlank(message = "Description is required")
    private String description;
}
