package com.landlens.property.repository;

import com.landlens.property.model.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    
    Optional<Property> findByPropertyCode(String propertyCode);
    
    boolean existsByPropertyCode(String propertyCode);

    List<Property> findByProviderIdAndIsActiveTrue(UUID providerId);

    @Query("SELECT p FROM Property p WHERE p.isActive = true " +
           "AND (:district IS NULL OR p.district LIKE %:district%) " +
           "AND (:village IS NULL OR p.village LIKE %:village%) " +
           "AND (:state IS NULL OR p.state LIKE %:state%) " +
           "AND (:status IS NULL OR p.status = :status) " +
           "AND (:category IS NULL OR p.category = :category) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Property> searchProperties(
            @Param("district") String district,
            @Param("village") String village,
            @Param("state") String state,
            @Param("status") String status,
            @Param("category") String category,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice);
}
