package com.landlens.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.landlens.common.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "api_rate_limits")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApiRateLimit extends BaseAuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"user", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private ApiKey apiKey;

    @Column(name = "limit_type", length = 20, nullable = false)
    private String limitType; // HOURLY, DAILY

    @Column(name = "max_requests", nullable = false)
    private Integer maxRequests;

    @Column(name = "current_window_start", nullable = false)
    private Instant currentWindowStart = Instant.now();
}
