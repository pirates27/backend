package com.landlens.property.repository;

import com.landlens.property.model.SavedProperty;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedPropertyRepository extends JpaRepository<SavedProperty, UUID> {
    List<SavedProperty> findByBuyerIdAndIsActiveTrue(UUID buyerId);
    Optional<SavedProperty> findByBuyerIdAndPropertyIdAndIsActiveTrue(UUID buyerId, UUID propertyId);
}
