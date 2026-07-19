package com.landlens.verification.service;

import com.landlens.property.model.Property;
import com.landlens.property.repository.PropertyRepository;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import com.landlens.verification.model.GovernmentVerification;
import com.landlens.verification.model.VerificationTimeline;
import com.landlens.verification.repository.GovernmentVerificationRepository;
import com.landlens.verification.repository.VerificationTimelineRepository;
import com.landlens.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class VerificationService {

    @Autowired
    private GovernmentVerificationRepository governmentVerificationRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private VerificationTimelineRepository timelineRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public GovernmentVerification verifyProperty(UUID propertyId, GovernmentVerification review, UUID officerId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        User officer = userRepository.findById(officerId)
                .orElseThrow(() -> new RuntimeException("Officer not found"));

        // Enforce role check
        if (!officer.getRole().getName().equals("GOVERNMENT_OFFICER") && !officer.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Unauthorized: User must be a Government Officer or Admin");
        }

        // Reuse existing verification if present, otherwise use the new review object
        GovernmentVerification finalReview = governmentVerificationRepository.findByPropertyIdAndIsActiveTrue(propertyId)
                .orElse(review);

        finalReview.setProperty(property);
        finalReview.setOfficer(officer);
        finalReview.setStatus(review.getStatus());
        finalReview.setRemarks(review.getRemarks());
        finalReview.setVerifiedDate(Instant.now());
        finalReview.setIsActive(true);

        GovernmentVerification savedReview = governmentVerificationRepository.save(finalReview);

        // Update Property Status
        property.setStatus(finalReview.getStatus().toUpperCase());
        propertyRepository.save(property);

        // Append to Timeline
        VerificationTimeline timeline = new VerificationTimeline();
        timeline.setProperty(property);
        timeline.setAction(review.getStatus().toUpperCase());
        timeline.setRemarks("Government review finished by officer: " + officer.getLastName() + ". Remarks: " + review.getRemarks());
        timeline.setUser(officer);
        timeline.setTimestamp(Instant.now());
        timeline.setIsActive(true);
        timelineRepository.save(timeline);

        // Send notification
        try {
            if (property.getProvider() != null) {
                notificationService.sendNotification(
                    property.getProvider().getId(),
                    "Government Verification Status Update",
                    "Your property \"" + property.getTitle() + "\" has been " + review.getStatus() + " by records officer " + officer.getLastName() + ". Remarks: " + review.getRemarks(),
                    "GOVT_AUDIT"
                );
            }
        } catch (Exception e) {
            // Ignore
        }

        return savedReview;
    }

    public List<VerificationTimeline> getPropertyTimeline(UUID propertyId) {
        return timelineRepository.findByPropertyIdAndIsActiveTrueOrderByTimestampAsc(propertyId);
    }
}
