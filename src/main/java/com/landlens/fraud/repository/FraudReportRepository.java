package com.landlens.fraud.repository;

import com.landlens.fraud.model.FraudReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FraudReportRepository extends JpaRepository<FraudReport, UUID> {
    List<FraudReport> findByPropertyId(UUID propertyId);
    List<FraudReport> findByStatus(String status);
    List<FraudReport> findByOfficerId(UUID officerId);
}
