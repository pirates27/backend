package com.landlens.verification.repository;

import com.landlens.verification.model.GovernmentVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface GovernmentVerificationRepository extends JpaRepository<GovernmentVerification, UUID> {
    Optional<GovernmentVerification> findByPropertyIdAndIsActiveTrue(UUID propertyId);
}
