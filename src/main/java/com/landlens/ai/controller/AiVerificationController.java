package com.landlens.ai.controller;

import com.landlens.ai.dto.AiVerificationResponseDto;
import com.landlens.ai.mapper.AiMapper;
import com.landlens.ai.model.AiVerification;
import com.landlens.ai.service.AiVerificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/properties")
@Transactional
public class AiVerificationController {

    @Autowired
    private AiVerificationService aiVerificationService;

    @PostMapping("/{id}/ai-verify")
    public ResponseEntity<AiVerificationResponseDto> triggerVerification(@PathVariable UUID id, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        AiVerification report = aiVerificationService.triggerAiVerification(id, userId);
        return ResponseEntity.ok(AiMapper.toResponseDto(report));
    }

    @GetMapping("/{id}/ai-verification")
    public ResponseEntity<AiVerificationResponseDto> getVerificationReport(@PathVariable UUID id) {
        AiVerification report = aiVerificationService.getReportByPropertyId(id);
        return ResponseEntity.ok(AiMapper.toResponseDto(report));
    }
}
