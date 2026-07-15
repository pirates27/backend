package com.landlens.api.model;

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

import java.time.Instant;

@Entity
@Table(name = "api_keys")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApiKey extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "role", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private User user;

    @Column(name = "key_hash", length = 255, nullable = false, unique = true)
    private String keyHash; // SHA-256 hash of API key

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "prefix", length = 8, nullable = false)
    private String prefix; // E.g., "ll_live_"

    @Column(name = "status", length = 20, nullable = false)
    private String status; // ACTIVE, REVOKED, EXPIRED

    @Column(name = "expiry_date")
    private Instant expiryDate;
}
