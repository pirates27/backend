package com.landlens.fraud.repository;

import com.landlens.fraud.model.DuplicateClaim;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DuplicateClaimRepository extends JpaRepository<DuplicateClaim, UUID> {
    List<DuplicateClaim> findByPropertyAIdOrPropertyBId(UUID propertyAId, UUID propertyBId);
}
