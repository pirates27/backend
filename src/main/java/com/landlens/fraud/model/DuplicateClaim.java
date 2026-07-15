package com.landlens.fraud.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.landlens.common.BaseAuditEntity;
import com.landlens.property.model.Property;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "duplicate_claims")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class DuplicateClaim extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_a_id", nullable = false)
    @JsonIgnoreProperties({"provider", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private Property propertyA;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_b_id", nullable = false)
    @JsonIgnoreProperties({"provider", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private Property propertyB;

    @Column(name = "similarity", precision = 5, scale = 2, nullable = false)
    private BigDecimal similarity;

    @Column(name = "reason", columnDefinition = "TEXT", nullable = false)
    private String reason;

    @Column(name = "status", length = 30, nullable = false)
    private String status; // FLAGGED, INVESTIGATING, RESOLVED, FALSE_POSITIVE

    @Column(name = "decision", length = 50)
    private String decision; // MERGED, CANCELLED_A, CANCELLED_B, NO_ACTION
}
