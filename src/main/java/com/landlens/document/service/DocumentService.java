package com.landlens.document.service;

import com.landlens.document.model.PropertyDocument;
import com.landlens.document.repository.PropertyDocumentRepository;
import com.landlens.property.model.Property;
import com.landlens.property.repository.PropertyRepository;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import com.landlens.verification.model.VerificationTimeline;
import com.landlens.verification.repository.VerificationTimelineRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class DocumentService {

    @Autowired
    private PropertyDocumentRepository documentRepository;

    @Autowired
    private PropertyRepository propertyRepository;

    @Autowired
    private VerificationTimelineRepository timelineRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public PropertyDocument uploadDocument(UUID propertyId, PropertyDocument doc, UUID userId) {
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new RuntimeException("Property not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        doc.setProperty(property);
        doc.setOcrStatus("PENDING");
        doc.setVerificationStatus("UNVERIFIED");
        doc.setIsActive(true);

        PropertyDocument savedDoc = documentRepository.save(doc);

        // Record timeline
        VerificationTimeline timeline = new VerificationTimeline();
        timeline.setProperty(property);
        timeline.setAction("UPLOADED");
        timeline.setRemarks("Document type " + doc.getDocumentType() + " uploaded by owner.");
        timeline.setUser(user);
        timeline.setTimestamp(Instant.now());
        timeline.setIsActive(true);
        timelineRepository.save(timeline);

        return savedDoc;
    }

    public List<PropertyDocument> getDocuments(UUID propertyId) {
        return documentRepository.findByPropertyIdAndIsActiveTrue(propertyId);
    }

    @Transactional
    public PropertyDocument triggerMockOcr(UUID docId, UUID userId) {
        PropertyDocument doc = documentRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("Document not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        doc.setOcrStatus("COMPLETED");
        doc.setVerificationStatus("VERIFIED");
        PropertyDocument savedDoc = documentRepository.save(doc);

        // Record timeline
        VerificationTimeline timeline = new VerificationTimeline();
        timeline.setProperty(doc.getProperty());
        timeline.setAction("AI_STARTED");
        timeline.setRemarks("OCR analysis completed. Verification status set to VERIFIED. Initiated AI Trust analysis.");
        timeline.setUser(user);
        timeline.setTimestamp(Instant.now());
        timeline.setIsActive(true);
        timelineRepository.save(timeline);

        return savedDoc;
    }
}
