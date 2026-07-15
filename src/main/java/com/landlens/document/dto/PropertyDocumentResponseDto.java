package com.landlens.document.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class PropertyDocumentResponseDto {
    private UUID id;
    private UUID propertyId;
    private String documentType;
    private String fileUrl;
    private String ocrStatus;
    private String verificationStatus;
    private Boolean isActive;
    private Instant createdAt;
    private Instant updatedAt;
}
