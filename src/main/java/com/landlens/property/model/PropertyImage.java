package com.landlens.property.model;

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

@Entity
@Table(name = "property_images")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PropertyImage extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnoreProperties({"provider", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private Property property;

    @Column(name = "image_url", length = 512, nullable = false)
    private String imageUrl;

    @Column(name = "thumbnail_url", length = 512, nullable = false)
    private String thumbnailUrl;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;
}
