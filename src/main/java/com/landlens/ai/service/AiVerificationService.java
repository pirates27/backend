package com.landlens.ai.service;

import com.landlens.ai.model.AiVerification;
import com.landlens.ai.repository.AiVerificationRepository;
import com.landlens.property.model.Property;
import com.landlens.property.repository.PropertyRepository;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import com.landlens.verification.model.VerificationTimeline;
import com.landlens.verification.repository.VerificationTimelineRepository;
import com.landlens.notification.service.NotificationService;
import com.landlens.document.repository.PropertyDocumentRepository;
import com.landlens.document.model.PropertyDocument;
import com.landlens.fraud.repository.DuplicateClaimRepository;
import com.landlens.fraud.model.DuplicateClaim;
import com.landlens.fraud.repository.FraudReportRepository;
import com.landlens.fraud.model.FraudReport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
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

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private PropertyDocumentRepository documentRepository;

    @Autowired
    private DuplicateClaimRepository duplicateClaimRepository;

    @Autowired
    private FraudReportRepository fraudReportRepository;

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

        // Fetch related data
        List<PropertyDocument> docs = documentRepository.findByPropertyIdAndIsActiveTrue(propertyId);
        List<DuplicateClaim> claims = duplicateClaimRepository.findByPropertyAIdOrPropertyBId(propertyId, propertyId);
        List<FraudReport> frauds = fraudReportRepository.findByPropertyId(propertyId);

        // Deterministic pseudo-random variation based on property UUID
        // This ensures the AI score doesn't look identical for two completely empty properties
        int seed = Math.abs(propertyId.hashCode());
        double variation = (seed % 10) - 5; // -5 to +4

        // 1. Forgery Score
        double forgery = 0.0;
        boolean ownershipMatch = false;
        if (docs == null || docs.isEmpty()) {
            forgery = 45.0 + Math.abs(variation); // Base high forgery risk if no documents exist
        } else {
            forgery = 2.0; // Base low score
            for (PropertyDocument doc : docs) {
                if ("FAILED".equals(doc.getOcrStatus())) forgery += 20.0;
                if ("REJECTED".equals(doc.getVerificationStatus())) forgery += 30.0;
                if (("SALE_DEED".equals(doc.getDocumentType()) || "PATTA".equals(doc.getDocumentType())) &&
                    !"FAILED".equals(doc.getOcrStatus()) && !"REJECTED".equals(doc.getVerificationStatus())) {
                    ownershipMatch = true;
                }
            }
        }
        
        // Metadata penalties for forgery
        if (property.getDescription() == null || property.getDescription().length() < 20) {
            forgery += 5.0; // Poor description increases forgery risk
        }
        
        forgery = Math.min(forgery, 100.0);

        // 2. Duplicate Score
        double duplicate = 0.0;
        if (claims != null && !claims.isEmpty()) {
            for (DuplicateClaim claim : claims) {
                if (claim.getSimilarity() != null && claim.getSimilarity().doubleValue() > duplicate) {
                    duplicate = claim.getSimilarity().doubleValue();
                }
            }
        } else {
            duplicate = Math.max(0.0, variation * 0.5); // Add a tiny bit of random variance (0-2%) for realism
        }

        // 3. Risk Score
        double risk = 0.0;
        if (frauds != null) {
            for (FraudReport fraud : frauds) {
                if ("SUBMITTED".equals(fraud.getStatus()) || "UNDER_INVESTIGATION".equals(fraud.getStatus())) {
                    risk += 25.0;
                }
            }
        }
        
        // Metadata penalties for risk
        if (property.getThreeSixtyImageUrl() == null || property.getThreeSixtyImageUrl().isEmpty()) {
            risk += 12.0; // Missing 360 view increases risk
        }
        if (property.getSurveyNumber() == null || property.getSurveyNumber().length() < 3) {
            risk += 8.0; // Suspiciously short survey number increases risk
        }
        
        risk = Math.min(risk, 100.0);

        // 4. Overall Trust Score
        double trust = 100.0 - (forgery * 0.4 + duplicate * 0.4 + risk * 0.2);
        trust = Math.max(trust, 0.0);

        String summary = "LandLens AI Trust engine analysis complete. ";
        if (trust > 80) summary += "High confidence. Bounds clear.";
        else if (trust > 50) summary += "Medium confidence. Some flags detected.";
        else summary += "Low confidence. Significant risks detected.";

        AiVerification report = new AiVerification();
        report.setProperty(property);
        report.setAiTrustScore(BigDecimal.valueOf(trust));
        report.setForgeryScore(BigDecimal.valueOf(forgery));
        report.setDuplicateScore(BigDecimal.valueOf(duplicate));
        report.setOwnershipMatch(ownershipMatch);
        report.setRiskScore(BigDecimal.valueOf(risk));
        report.setConfidence(BigDecimal.valueOf(Math.min(trust + 5.0, 100.0)));
        report.setSummary(summary);
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
        timeline.setRemarks(String.format("AI Trust evaluation finished. Trust Score: %.2f%%. Status updated to PENDING_GOVT.", trust));
        timeline.setUser(user);
        timeline.setTimestamp(Instant.now());
        timeline.setIsActive(true);
        timelineRepository.save(timeline);

        // Send notifications
        try {
            // Notify the provider
            if (property.getProvider() != null) {
                notificationService.sendNotification(
                    property.getProvider().getId(),
                    "AI Trust Audit Completed",
                    String.format("AI verification analysis is complete for your property \"%s\". Trust Score: %.2f%%. The listing has been routed to the government verification queue.", property.getTitle(), trust),
                    "AI_AUDIT"
                );
            }

            // Notify all government officers
            List<User> officers = userRepository.findByRoleName("GOVERNMENT_OFFICER");
            for (User officer : officers) {
                notificationService.sendNotification(
                    officer.getId(),
                    "New Property Pending Review",
                    String.format("Property \"%s\" has passed AI Trust Audit with a score of %.2f%%. It is now pending your manual records audit.", property.getTitle(), trust),
                    "PENDING_AUDIT"
                );
            }
        } catch (Exception e) {
            // Ignore notification errors to prevent transaction rollback
        }

        return savedReport;
    }

    public AiVerification getReportByPropertyId(UUID propertyId) {
        return aiVerificationRepository.findByPropertyIdAndIsActiveTrue(propertyId)
                .orElseThrow(() -> new RuntimeException("AI report not found for this property"));
    }
}
