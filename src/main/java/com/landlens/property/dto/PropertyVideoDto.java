package com.landlens.property.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class PropertyVideoDto {
    private UUID id;
    private UUID propertyId;
    private String videoUrl;
    private Integer duration;
    private String thumbnailUrl;
}
