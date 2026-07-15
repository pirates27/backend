package com.landlens.document.model;

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

@Entity
@Table(name = "property_documents")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class PropertyDocument extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "property_id", nullable = false)
    @JsonIgnoreProperties({"provider", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private Property property;

    @Column(name = "document_type", length = 50, nullable = false)
    private String documentType; // SALE_DEED, PATTA, SURVEY_MAP, TAX_RECEIPT, IDENTITY_PROOF, OWNERSHIP_PROOF

    @Column(name = "file_url", length = 512, nullable = false)
    private String fileUrl;

    @Column(name = "ocr_status", length = 30, nullable = false)
    private String ocrStatus; // PENDING, PROCESSING, COMPLETED, FAILED

    @Column(name = "verification_status", length = 30, nullable = false)
    private String verificationStatus; // UNVERIFIED, VERIFIED, REJECTED
}
