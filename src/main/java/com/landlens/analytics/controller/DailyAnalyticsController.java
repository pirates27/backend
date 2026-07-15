package com.landlens.analytics.controller;

import com.landlens.analytics.dto.DailyAnalyticsResponseDto;
import com.landlens.analytics.mapper.AnalyticsMapper;
import com.landlens.analytics.model.DailyAnalytics;
import com.landlens.analytics.service.DailyAnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/analytics")
@Transactional
public class DailyAnalyticsController {

    @Autowired
    private DailyAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DailyAnalyticsResponseDto> getDashboardMetrics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        
        LocalDate targetDate = date != null ? date : LocalDate.now();
        DailyAnalytics metrics = analyticsService.getDailyAnalyticsForDate(targetDate);
        return ResponseEntity.ok(AnalyticsMapper.toResponseDto(metrics));
    }
}
