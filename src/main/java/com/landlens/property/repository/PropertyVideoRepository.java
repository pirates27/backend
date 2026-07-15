package com.landlens.property.repository;

import com.landlens.property.model.PropertyVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyVideoRepository extends JpaRepository<PropertyVideo, UUID> {
    List<PropertyVideo> findByPropertyIdAndIsActiveTrue(UUID propertyId);
}
