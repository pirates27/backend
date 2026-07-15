package com.landlens.property.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.landlens.common.BaseAuditEntity;
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

import java.math.BigDecimal;

@Entity
@Table(name = "properties")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Property extends BaseAuditEntity {

    @Column(name = "property_code", length = 50, nullable = false, unique = true)
    private String propertyCode;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "category", length = 50, nullable = false)
    private String category; // RESIDENTIAL, COMMERCIAL, AGRICULTURAL, INDUSTRIAL

    @Column(name = "area", precision = 12, scale = 2, nullable = false)
    private BigDecimal area;

    @Column(name = "price", precision = 15, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "survey_number", length = 50, nullable = false)
    private String surveyNumber;

    @Column(name = "address", length = 255, nullable = false)
    private String address;

    @Column(name = "latitude", precision = 9, scale = 6, nullable = false)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6, nullable = false)
    private BigDecimal longitude;

    @Column(name = "district", length = 100, nullable = false)
    private String district;

    @Column(name = "village", length = 100, nullable = false)
    private String village;

    @Column(name = "state", length = 100, nullable = false)
    private String state;

    @Column(name = "pincode", length = 10, nullable = false)
    private String pincode;

    @Column(name = "three_sixty_image_url", length = 512)
    private String threeSixtyImageUrl;

    @Column(name = "status", length = 30, nullable = false)
    private String status; // PENDING_AI, PENDING_GOVT, APPROVED, REJECTED, DISPUTED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "provider_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "role", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private User provider;
}
