package com.landlens.property.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
public class PropertyVisitRequestDto {
    @NotNull(message = "Visit date is required")
    private LocalDate visitDate;

    @NotNull(message = "Visit time is required")
    private LocalTime visitTime;
}
