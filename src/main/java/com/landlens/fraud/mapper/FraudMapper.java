package com.landlens.fraud.mapper;

import com.landlens.fraud.dto.DuplicateClaimRequestDto;
import com.landlens.fraud.dto.DuplicateClaimResponseDto;
import com.landlens.fraud.dto.FraudReportRequestDto;
import com.landlens.fraud.dto.FraudReportResponseDto;
import com.landlens.fraud.model.DuplicateClaim;
import com.landlens.fraud.model.FraudReport;
import com.landlens.property.model.Property;
import com.landlens.user.mapper.UserMapper;

public class FraudMapper {

    public static FraudReportResponseDto toResponseDto(FraudReport report) {
        if (report == null) {
            return null;
        }
        FraudReportResponseDto dto = new FraudReportResponseDto();
        dto.setId(report.getId());
        dto.setReporter(UserMapper.toResponseDto(report.getReporter()));
        if (report.getProperty() != null) {
            dto.setPropertyId(report.getProperty().getId());
        }
        dto.setReason(report.getReason());
        dto.setDescription(report.getDescription());
        dto.setStatus(report.getStatus());
        dto.setOfficer(UserMapper.toResponseDto(report.getOfficer()));
        dto.setIsActive(report.getIsActive());
        dto.setCreatedAt(report.getCreatedAt());
        dto.setUpdatedAt(report.getUpdatedAt());
        return dto;
    }

    public static FraudReport toEntity(FraudReportRequestDto dto) {
        if (dto == null) {
            return null;
        }
        FraudReport report = new FraudReport();
        report.setReason(dto.getReason());
        report.setDescription(dto.getDescription());
        return report;
    }

    public static DuplicateClaimResponseDto toResponseDto(DuplicateClaim claim) {
        if (claim == null) {
            return null;
        }
        DuplicateClaimResponseDto dto = new DuplicateClaimResponseDto();
        dto.setId(claim.getId());
        if (claim.getPropertyA() != null) {
            dto.setPropertyAId(claim.getPropertyA().getId());
        }
        if (claim.getPropertyB() != null) {
            dto.setPropertyBId(claim.getPropertyB().getId());
        }
        dto.setSimilarity(claim.getSimilarity());
        dto.setReason(claim.getReason());
        dto.setStatus(claim.getStatus());
        dto.setDecision(claim.getDecision());
        dto.setIsActive(claim.getIsActive());
        dto.setCreatedAt(claim.getCreatedAt());
        return dto;
    }

    public static DuplicateClaim toEntity(DuplicateClaimRequestDto dto, Property propertyA, Property propertyB) {
        if (dto == null) {
            return null;
        }
        DuplicateClaim claim = new DuplicateClaim();
        claim.setPropertyA(propertyA);
        claim.setPropertyB(propertyB);
        claim.setSimilarity(dto.getSimilarity());
        claim.setReason(dto.getReason());
        claim.setStatus(dto.getStatus() != null ? dto.getStatus() : "FLAGGED");
        claim.setDecision(dto.getDecision());
        claim.setIsActive(true);
        return claim;
    }
}
