package com.landlens.analytics.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
public class DailyAnalyticsResponseDto {
    private UUID id;
    private LocalDate analyticsDate;
    private Integer propertyViews;
    private Integer searchCount;
    private Integer verificationCount;
    private Integer fraudCount;
    private Integer apiCalls;
}
