package com.landlens.ai.repository;

import com.landlens.ai.model.AiVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AiVerificationRepository extends JpaRepository<AiVerification, UUID> {
    Optional<AiVerification> findByPropertyIdAndIsActiveTrue(UUID propertyId);
}
