package com.landlens.property.dto;

import com.landlens.user.dto.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

@Getter
@Setter
public class PropertyVisitResponseDto {
    private UUID id;
    private UserResponseDto buyer;
    private PropertyResponseDto property;
    private LocalDate visitDate;
    private LocalTime visitTime;
    private String status;
}
