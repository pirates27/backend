package com.landlens.analytics.mapper;

import com.landlens.analytics.dto.DailyAnalyticsResponseDto;
import com.landlens.analytics.model.DailyAnalytics;

public class AnalyticsMapper {

    private AnalyticsMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static DailyAnalyticsResponseDto toResponseDto(DailyAnalytics analytics) {
        if (analytics == null) {
            return null;
        }
        DailyAnalyticsResponseDto dto = new DailyAnalyticsResponseDto();
        dto.setId(analytics.getId());
        dto.setAnalyticsDate(analytics.getAnalyticsDate());
        dto.setPropertyViews(analytics.getPropertyViews());
        dto.setSearchCount(analytics.getSearchCount());
        dto.setVerificationCount(analytics.getVerificationCount());
        dto.setFraudCount(analytics.getFraudCount());
        dto.setApiCalls(analytics.getApiCalls());
        return dto;
    }
}
