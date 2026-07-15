package com.landlens.property.repository;

import com.landlens.property.model.PropertyVisit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyVisitRepository extends JpaRepository<PropertyVisit, UUID> {
    List<PropertyVisit> findByBuyerIdAndIsActiveTrue(UUID buyerId);
    List<PropertyVisit> findByPropertyProviderIdAndIsActiveTrue(UUID providerId);
}
