package com.landlens.property.dto;

import com.landlens.user.dto.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class SavedPropertyResponseDto {
    private UUID id;
    private UserResponseDto buyer;
    private PropertyResponseDto property;
}
