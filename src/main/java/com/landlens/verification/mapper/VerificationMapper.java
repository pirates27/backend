package com.landlens.verification.mapper;

import com.landlens.user.mapper.UserMapper;
import com.landlens.verification.dto.GovernmentVerificationResponseDto;
import com.landlens.verification.dto.VerificationTimelineResponseDto;
import com.landlens.verification.model.GovernmentVerification;
import com.landlens.verification.model.VerificationTimeline;

public class VerificationMapper {

    public static GovernmentVerificationResponseDto toResponseDto(GovernmentVerification review) {
        if (review == null) {
            return null;
        }
        GovernmentVerificationResponseDto dto = new GovernmentVerificationResponseDto();
        dto.setId(review.getId());
        if (review.getProperty() != null) {
            dto.setPropertyId(review.getProperty().getId());
        }
        dto.setOfficer(UserMapper.toResponseDto(review.getOfficer()));
        dto.setRemarks(review.getRemarks());
        dto.setStatus(review.getStatus());
        dto.setVerifiedDate(review.getVerifiedDate());
        return dto;
    }

    public static VerificationTimelineResponseDto toResponseDto(VerificationTimeline timeline) {
        if (timeline == null) {
            return null;
        }
        VerificationTimelineResponseDto dto = new VerificationTimelineResponseDto();
        dto.setId(timeline.getId());
        if (timeline.getProperty() != null) {
            dto.setPropertyId(timeline.getProperty().getId());
        }
        dto.setTimestamp(timeline.getTimestamp());
        dto.setAction(timeline.getAction());
        dto.setRemarks(timeline.getRemarks());
        dto.setUser(UserMapper.toResponseDto(timeline.getUser()));
        return dto;
    }
}
