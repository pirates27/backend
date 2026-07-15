package com.landlens.api.repository;

import com.landlens.api.model.ApiRateLimit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ApiRateLimitRepository extends JpaRepository<ApiRateLimit, UUID> {
    Optional<ApiRateLimit> findByApiKeyId(UUID apiKeyId);
}
