package com.landlens.document.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PropertyDocumentRequestDto {
    @NotBlank(message = "Document type is required")
    private String documentType;

    @NotBlank(message = "File URL is required")
    private String fileUrl;
}
