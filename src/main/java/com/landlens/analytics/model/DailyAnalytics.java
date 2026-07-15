package com.landlens.analytics.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.landlens.common.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "daily_analytics")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DailyAnalytics extends BaseAuditEntity {

    @Column(name = "analytics_date", nullable = false, unique = true)
    private LocalDate analyticsDate;

    @Column(name = "property_views", nullable = false)
    private Integer propertyViews = 0;

    @Column(name = "search_count", nullable = false)
    private Integer searchCount = 0;

    @Column(name = "verification_count", nullable = false)
    private Integer verificationCount = 0;

    @Column(name = "fraud_count", nullable = false)
    private Integer fraudCount = 0;

    @Column(name = "api_calls", nullable = false)
    private Integer apiCalls = 0;
}
