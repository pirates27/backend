package com.landlens.document.mapper;

import com.landlens.document.dto.PropertyDocumentRequestDto;
import com.landlens.document.dto.PropertyDocumentResponseDto;
import com.landlens.document.model.PropertyDocument;

public class DocumentMapper {

    public static PropertyDocumentResponseDto toResponseDto(PropertyDocument doc) {
        if (doc == null) {
            return null;
        }
        PropertyDocumentResponseDto dto = new PropertyDocumentResponseDto();
        dto.setId(doc.getId());
        if (doc.getProperty() != null) {
            dto.setPropertyId(doc.getProperty().getId());
        }
        dto.setDocumentType(doc.getDocumentType());
        dto.setFileUrl(doc.getFileUrl());
        dto.setOcrStatus(doc.getOcrStatus());
        dto.setVerificationStatus(doc.getVerificationStatus());
        dto.setIsActive(doc.getIsActive());
        dto.setCreatedAt(doc.getCreatedAt());
        dto.setUpdatedAt(doc.getUpdatedAt());
        return dto;
    }

    public static PropertyDocument toEntity(PropertyDocumentRequestDto dto) {
        if (dto == null) {
            return null;
        }
        PropertyDocument doc = new PropertyDocument();
        doc.setDocumentType(dto.getDocumentType());
        doc.setFileUrl(dto.getFileUrl());
        return doc;
    }
}
