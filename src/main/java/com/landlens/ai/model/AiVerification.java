package com.landlens.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.landlens.common.BaseAuditEntity;
import com.landlens.property.model.Property;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ai_verifications")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AiVerification extends BaseAuditEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false, unique = true)
    @JsonIgnoreProperties({"provider", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private Property property;

    @Column(name = "ai_trust_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal aiTrustScore;

    @Column(name = "forgery_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal forgeryScore;

    @Column(name = "duplicate_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal duplicateScore;

    @Column(name = "ownership_match", nullable = false)
    private Boolean ownershipMatch;

    @Column(name = "risk_score", precision = 5, scale = 2, nullable = false)
    private BigDecimal riskScore;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "reasoning", columnDefinition = "TEXT")
    private String reasoning;

    @Column(name = "confidence", precision = 5, scale = 2, nullable = false)
    private BigDecimal confidence;

    @Column(name = "generated_date", nullable = false)
    private Instant generatedDate = Instant.now();
}
