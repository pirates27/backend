package com.landlens.ai.service;

import com.landlens.ai.model.AiVerification;
import com.landlens.ai.repository.AiVerificationRepository;
import com.landlens.property.model.Property;
import com.landlens.property.repository.PropertyRepository;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import com.landlens.verification.model.VerificationTimeline;
import com.landlens.verification.repository.VerificationTimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Service
public class AiVerificationService {

    @Autowired
    private AiVerificationRepository aiVerificationRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private VerificationTimelineRepository timelineRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public AiVerification triggerAiVerification(UUID propertyId, UUID userId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Delete any existing AI report for this property
        aiVerificationRepository.findByPropertyIdAndIsActiveTrue(propertyId).ifPresent(existing -> {
            existing.setIsActive(false);
            aiVerificationRepository.save(existing);
        });

        // Seed some mock verification scores
        AiVerification report = new AiVerification();
        report.setProperty(property);
        report.setAiTrustScore(new BigDecimal("88.50"));
        report.setForgeryScore(new BigDecimal("5.20"));
        report.setDuplicateScore(new BigDecimal("0.00"));
        report.setOwnershipMatch(true);
        report.setRiskScore(new BigDecimal("8.00"));
        report.setConfidence(new BigDecimal("96.20"));
        report.setSummary("LandLens AI Trust engine analysis complete. High confidence ownership match. Bounds clear.");
        report.setIsActive(true);
        report.setGeneratedDate(Instant.now());

        AiVerification savedReport = aiVerificationRepository.save(report);

        // Update Property Status
        property.setStatus("PENDING_GOVT");
        propertyRepository.save(property);

        // Log Timeline
        VerificationTimeline timeline = new VerificationTimeline();
        timeline.setProperty(property);
        timeline.setAction("AI_COMPLETED");
        timeline.setRemarks("AI Trust evaluation finished. Trust Score: 88.50%. Status updated to PENDING_GOVT.");
        timeline.setUser(user);
        timeline.setTimestamp(Instant.now());
        timeline.setIsActive(true);
        timelineRepository.save(timeline);

        return savedReport;
    }

    public AiVerification getReportByPropertyId(UUID propertyId) {
        return aiVerificationRepository.findByPropertyIdAndIsActiveTrue(propertyId)
                .orElseThrow(() -> new RuntimeException("AI report not found for this property"));
    }
}
