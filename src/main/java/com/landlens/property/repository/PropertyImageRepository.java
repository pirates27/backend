package com.landlens.property.repository;

import com.landlens.property.model.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, UUID> {
    List<PropertyImage> findByPropertyIdAndIsActiveTrueOrderByDisplayOrderAsc(UUID propertyId);
}
