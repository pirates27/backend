package com.landlens.document.repository;

import com.landlens.document.model.PropertyDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyDocumentRepository extends JpaRepository<PropertyDocument, UUID> {
    List<PropertyDocument> findByPropertyIdAndIsActiveTrue(UUID propertyId);
}
