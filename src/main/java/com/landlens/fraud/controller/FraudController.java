package com.landlens.fraud.controller;

import com.landlens.fraud.dto.DuplicateClaimRequestDto;
import com.landlens.fraud.dto.DuplicateClaimResponseDto;
import com.landlens.fraud.dto.FraudReportRequestDto;
import com.landlens.fraud.dto.FraudReportResponseDto;
import com.landlens.fraud.mapper.FraudMapper;
import com.landlens.fraud.model.DuplicateClaim;
import com.landlens.fraud.model.FraudReport;
import com.landlens.fraud.service.FraudService;
import com.landlens.property.model.Property;
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
public class FraudController {

    @Autowired
    private FraudService fraudService;

    @PostMapping("/properties/{id}/fraud-reports")
    public ResponseEntity<FraudReportResponseDto> submitReport(
            @PathVariable UUID id,
            @Valid @RequestBody FraudReportRequestDto reportDto,
            Principal principal) {
        UUID reporterId = UUID.fromString(principal.getName());
        FraudReport report = FraudMapper.toEntity(reportDto);
        FraudReport submitted = fraudService.submitReport(id, report, reporterId);
        return ResponseEntity.ok(FraudMapper.toResponseDto(submitted));
    }

    @GetMapping("/fraud-reports")
    public ResponseEntity<List<FraudReportResponseDto>> getAllReports() {
        List<FraudReport> list = fraudService.getAllReports();
        List<FraudReportResponseDto> dtoList = list.stream()
                .map(FraudMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @GetMapping("/properties/{id}/fraud-reports")
    public ResponseEntity<List<FraudReportResponseDto>> getReportsForProperty(@PathVariable UUID id) {
        List<FraudReport> list = fraudService.getReportsForProperty(id);
        List<FraudReportResponseDto> dtoList = list.stream()
                .map(FraudMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/fraud-reports/{id}/assign")
    @PreAuthorize("hasAnyRole('GOVERNMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<FraudReportResponseDto> assignOfficer(
            @PathVariable UUID id,
            @RequestParam UUID officerId) {
        FraudReport updated = fraudService.assignOfficer(id, officerId);
        return ResponseEntity.ok(FraudMapper.toResponseDto(updated));
    }

    @PutMapping("/fraud-reports/{id}/resolve")
    @PreAuthorize("hasAnyRole('GOVERNMENT_OFFICER', 'ADMIN')")
    public ResponseEntity<FraudReportResponseDto> resolveReport(
            @PathVariable UUID id,
            @RequestParam String status) {
        FraudReport updated = fraudService.resolveReport(id, status);
        return ResponseEntity.ok(FraudMapper.toResponseDto(updated));
    }

    @PostMapping("/duplicate-claims")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<DuplicateClaimResponseDto> createDuplicateClaim(
            @Valid @RequestBody DuplicateClaimRequestDto claimDto) {
        Property propA = new Property();
        propA.setId(claimDto.getPropertyAId());
        Property propB = new Property();
        propB.setId(claimDto.getPropertyBId());

        DuplicateClaim claim = FraudMapper.toEntity(claimDto, propA, propB);
        DuplicateClaim created = fraudService.createDuplicateClaim(claim);
        return ResponseEntity.ok(FraudMapper.toResponseDto(created));
    }

    @GetMapping("/properties/{id}/duplicate-claims")
    public ResponseEntity<List<DuplicateClaimResponseDto>> getDuplicateClaims(@PathVariable UUID id) {
        List<DuplicateClaim> list = fraudService.getDuplicateClaimsForProperty(id);
        List<DuplicateClaimResponseDto> dtoList = list.stream()
                .map(FraudMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }
}
