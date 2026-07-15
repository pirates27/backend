package com.landlens.fraud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.landlens.common.BaseAuditEntity;
import com.landlens.property.model.Property;
import com.landlens.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "fraud_reports")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class FraudReport extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "role", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnoreProperties({"provider", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private Property property;

    @Column(name = "reason", length = 150, nullable = false)
    private String reason;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "status", length = 30, nullable = false)
    private String status; // SUBMITTED, UNDER_INVESTIGATION, RESOLVED_FRAUD, RESOLVED_DISMISSED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "officer_id")
    @JsonIgnoreProperties({"passwordHash", "role", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private User officer; // Investigating officer (nullable initially)
}
