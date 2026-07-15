package com.landlens.property.dto;

import com.landlens.user.dto.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class PropertyResponseDto {
    private UUID id;
    private String propertyCode;
    private String title;
    private String category;
    private BigDecimal area;
    private BigDecimal price;
    private String description;
    private String surveyNumber;
    private String address;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String district;
    private String village;
    private String state;
    private String pincode;
    private String threeSixtyImageUrl;
    private String status;
    private UserResponseDto provider;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
