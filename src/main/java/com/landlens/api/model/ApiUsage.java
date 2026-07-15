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

import java.time.LocalDate;

@Entity
@Table(name = "api_usages")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApiUsage extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "api_key_id", nullable = false)
    @JsonIgnoreProperties({"user", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private ApiKey apiKey;

    @Column(name = "usage_date", nullable = false)
    private LocalDate usageDate;

    @Column(name = "call_count", nullable = false)
    private Integer callCount = 0;
}
