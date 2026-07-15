package com.landlens.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.landlens.common.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "api_logs")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApiLog extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id", nullable = false)
    @JsonIgnoreProperties({"user", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private ApiKey apiKey;

    @Column(name = "endpoint", length = 255, nullable = false)
    private String endpoint;

    @Column(name = "method", length = 10, nullable = false)
    private String method; // GET, POST, PUT, DELETE

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @Column(name = "ip_address", length = 45, nullable = false)
    private String ipAddress;

    @Column(name = "request_timestamp", nullable = false)
    private Instant requestTimestamp = Instant.now();

    @Column(name = "response_time_ms", nullable = false)
    private Integer responseTimeMs;
}
