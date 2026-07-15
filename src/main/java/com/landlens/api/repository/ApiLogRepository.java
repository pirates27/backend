package com.landlens.api.repository;

import com.landlens.api.model.ApiLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ApiLogRepository extends JpaRepository<ApiLog, UUID> {
    List<ApiLog> findByApiKeyId(UUID apiKeyId);
}
