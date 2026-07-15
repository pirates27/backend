package com.landlens.api.repository;

import com.landlens.api.model.ApiUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiUsageRepository extends JpaRepository<ApiUsage, UUID> {
    Optional<ApiUsage> findByApiKeyIdAndUsageDate(UUID apiKeyId, LocalDate usageDate);
}
