package com.landlens.document.controller;

import com.landlens.document.dto.PropertyDocumentRequestDto;
import com.landlens.document.dto.PropertyDocumentResponseDto;
import com.landlens.document.mapper.DocumentMapper;
import com.landlens.document.model.PropertyDocument;
import com.landlens.document.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@Transactional
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @PostMapping("/properties/{id}/documents")
    @PreAuthorize("hasAnyRole('PROVIDER', 'ADMIN')")
    public ResponseEntity<PropertyDocumentResponseDto> uploadDocument(
            @PathVariable UUID id,
            @Valid @RequestBody PropertyDocumentRequestDto docDto,
            Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        PropertyDocument doc = DocumentMapper.toEntity(docDto);
        PropertyDocument uploaded = documentService.uploadDocument(id, doc, userId);
        return ResponseEntity.ok(DocumentMapper.toResponseDto(uploaded));
    }

    @GetMapping("/properties/{id}/documents")
    public ResponseEntity<List<PropertyDocumentResponseDto>> getDocuments(@PathVariable UUID id) {
        List<PropertyDocument> list = documentService.getDocuments(id);
        List<PropertyDocumentResponseDto> dtoList = list.stream()
                .map(DocumentMapper::toResponseDto)
                .toList();
        return ResponseEntity.ok(dtoList);
    }

    @PostMapping("/documents/{docId}/ocr")
    public ResponseEntity<PropertyDocumentResponseDto> triggerOcr(@PathVariable UUID docId, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        PropertyDocument doc = documentService.triggerMockOcr(docId, userId);
        return ResponseEntity.ok(DocumentMapper.toResponseDto(doc));
    }
}
