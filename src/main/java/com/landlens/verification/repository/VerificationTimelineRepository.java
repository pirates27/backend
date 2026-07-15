package com.landlens.verification.repository;

import com.landlens.verification.model.VerificationTimeline;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface VerificationTimelineRepository extends JpaRepository<VerificationTimeline, UUID> {
    List<VerificationTimeline> findByPropertyIdAndIsActiveTrueOrderByTimestampAsc(UUID propertyId);
}
