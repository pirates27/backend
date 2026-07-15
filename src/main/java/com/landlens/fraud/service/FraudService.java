package com.landlens.fraud.service;

import com.landlens.fraud.model.DuplicateClaim;
import com.landlens.fraud.model.FraudReport;
import com.landlens.fraud.repository.DuplicateClaimRepository;
import com.landlens.fraud.repository.FraudReportRepository;
import com.landlens.property.model.Property;
import com.landlens.property.repository.PropertyRepository;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FraudService {

    @Autowired
    private DuplicateClaimRepository duplicateClaimRepository;

    @Autowired
    private FraudReportRepository fraudReportRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public FraudReport submitReport(UUID propertyId, FraudReport report, UUID reporterId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        User reporter = userRepository.findById(reporterId)
                .orElseThrow(() -> new RuntimeException("Reporter user not found"));

        report.setProperty(property);
        report.setReporter(reporter);
        report.setStatus("SUBMITTED");
        report.setIsActive(true);

        return fraudReportRepository.save(report);
    }

    @Transactional
    public FraudReport assignOfficer(UUID reportId, UUID officerId) {
        FraudReport report = fraudReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Fraud report not found"));
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        if (!officer.getRole().getName().equals("GOVERNMENT_OFFICER") && !officer.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Assignee must be a Government Officer or Admin");
        }

        report.setOfficer(officer);
        report.setStatus("UNDER_INVESTIGATION");
        return fraudReportRepository.save(report);
    }

    @Transactional
    public FraudReport resolveReport(UUID reportId, String status) {
        FraudReport report = fraudReportRepository.findById(reportId)
                .orElseThrow(() -> new RuntimeException("Fraud report not found"));

        // Valid statuses: RESOLVED_FRAUD, RESOLVED_DISMISSED
        report.setStatus(status.toUpperCase());
        
        if (status.equalsIgnoreCase("RESOLVED_FRAUD")) {
            // Update property status to DISPUTED
            Property property = report.getProperty();
            property.setStatus("DISPUTED");
            propertyRepository.save(property);
        }

        return fraudReportRepository.save(report);
    }

    public List<FraudReport> getAllReports() {
        return fraudReportRepository.findAll();
    }

    public List<FraudReport> getReportsForProperty(UUID propertyId) {
        return fraudReportRepository.findByPropertyId(propertyId);
    }

    @Transactional
    public DuplicateClaim createDuplicateClaim(DuplicateClaim claim) {
        // Enforce validations if properties exist
        propertyRepository.findById(claim.getPropertyA().getId())
                .orElseThrow(() -> new RuntimeException("Property A not found"));
        propertyRepository.findById(claim.getPropertyB().getId())
                .orElseThrow(() -> new RuntimeException("Property B not found"));

        claim.setStatus("FLAGGED");
        claim.setIsActive(true);
        return duplicateClaimRepository.save(claim);
    }

    public List<DuplicateClaim> getDuplicateClaimsForProperty(UUID propertyId) {
        return duplicateClaimRepository.findByPropertyAIdOrPropertyBId(propertyId, propertyId);
    }
}
