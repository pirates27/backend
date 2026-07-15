package com.landlens.verification.controller;

import com.landlens.verification.dto.GovernmentVerificationRequestDto;
import com.landlens.verification.dto.GovernmentVerificationResponseDto;
import com.landlens.verification.dto.VerificationTimelineResponseDto;
import com.landlens.verification.mapper.VerificationMapper;
import com.landlens.verification.model.GovernmentVerification;
import com.landlens.verification.model.VerificationTimeline;
import com.landlens.verification.service.VerificationService;
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
@RequestMapping("/api/properties")
@Transactional
public class VerificationController {

    @Autowired
    private VerificationService verificationService;

    @PostMapping("/{id}/government-verify")
    @PreAuthorize("hasAnyRole('GOVERNMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<GovernmentVerificationResponseDto> verifyProperty(
            @PathVariable UUID id,
            @Valid @RequestBody GovernmentVerificationRequestDto reviewDto,
            Principal principal) {
        UUID officerId = UUID.fromString(principal.getName());
        GovernmentVerification review = new GovernmentVerification();
        review.setStatus(reviewDto.getStatus());
        review.setRemarks(reviewDto.getRemarks());

        GovernmentVerification saved = verificationService.verifyProperty(id, review, officerId);
        return ResponseEntity.ok(VerificationMapper.toResponseDto(saved));
    }

    @GetMapping("/{id}/timeline")
    public ResponseEntity<List<VerificationTimelineResponseDto>> getPropertyTimeline(@PathVariable UUID id) {
        List<VerificationTimeline> list = verificationService.getPropertyTimeline(id);
        List<VerificationTimelineResponseDto> dtoList = list.stream()
                .map(VerificationMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
}
