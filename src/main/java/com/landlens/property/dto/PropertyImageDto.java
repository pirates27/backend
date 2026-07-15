package com.landlens.property.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PropertyImageDto {
    private UUID id;
    private UUID propertyId;
    private String imageUrl;
    private String thumbnailUrl;
    private Integer displayOrder;
}
