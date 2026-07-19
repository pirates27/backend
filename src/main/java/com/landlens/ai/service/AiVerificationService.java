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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

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

        // Call NVIDIA LLM for verification
        double trust = 0.0;
        double forgery = 0.0;
        double duplicate = 0.0;
        double risk = 0.0;
        boolean ownershipMatch = false;
        double confidence = 0.0;
        String summary = "";
        String reasoning = "";

        try {
            ObjectMapper mapper = new ObjectMapper();
            
            // Construct context string
            StringBuilder context = new StringBuilder();
            context.append("Property Title: ").append(property.getTitle()).append("\n");
            context.append("Property Desc: ").append(property.getDescription()).append("\n");
            context.append("Survey No: ").append(property.getSurveyNumber()).append("\n");
            context.append("Has 360 Image: ").append(property.getThreeSixtyImageUrl() != null).append("\n");
            
            context.append("Documents:\n");
            if (docs == null || docs.isEmpty()) {
                context.append("NONE\n");
            } else {
                for (PropertyDocument doc : docs) {
                    context.append("- ").append(doc.getDocumentType()).append(" (OCR: ").append(doc.getOcrStatus()).append(", VERIFY: ").append(doc.getVerificationStatus()).append(")\n");
                }
            }
            
            context.append("Fraud Reports: ").append(frauds != null ? frauds.size() : 0).append("\n");
            context.append("Duplicate Claims: ").append(claims != null ? claims.size() : 0).append("\n");

            // Build request JSON
            ObjectNode rootNode = mapper.createObjectNode();
            rootNode.put("model", "openai/gpt-oss-120b");
            rootNode.put("temperature", 1);
            rootNode.put("top_p", 1);
            rootNode.put("max_tokens", 1024);
            rootNode.put("stream", false);
            
            ArrayNode messagesArray = rootNode.putArray("messages");
            ObjectNode messageNode = mapper.createObjectNode();
            messageNode.put("role", "user");
            messageNode.put("content", "You are an expert real estate auditor. Analyze the following property data and return your evaluation strictly as a JSON object with NO OTHER TEXT or markdown. Keys must be: 'aiTrustScore' (0-100 float), 'forgeryScore' (0-100 float), 'duplicateScore' (0-100 float), 'riskScore' (0-100 float), 'ownershipMatch' (boolean), 'confidence' (0-100 float), 'summary' (string). Data:\n" + context.toString());
            messagesArray.add(messageNode);

            String requestBody = mapper.writeValueAsString(rootNode);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://integrate.api.nvidia.com/v1/chat/completions"))
                    .header("Authorization", "Bearer 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")
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                JsonNode responseNode = mapper.readTree(response.body());
                String content = responseNode.path("choices").get(0).path("message").path("content").asText();
                
                // Clean up possible markdown code blocks
                if (content.startsWith("```json")) {
                    content = content.substring(7);
                    if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
                } else if (content.startsWith("```")) {
                    content = content.substring(3);
                    if (content.endsWith("```")) content = content.substring(0, content.length() - 3);
                }
                
                JsonNode llmResult = mapper.readTree(content.trim());
                trust = llmResult.path("aiTrustScore").asDouble(50.0);
                forgery = llmResult.path("forgeryScore").asDouble(50.0);
                duplicate = llmResult.path("duplicateScore").asDouble(0.0);
                risk = llmResult.path("riskScore").asDouble(0.0);
                ownershipMatch = llmResult.path("ownershipMatch").asBoolean(false);
                confidence = llmResult.path("confidence").asDouble(50.0);
                summary = llmResult.path("summary").asText("LLM verification complete.");
                
                // Extract reasoning trace if available
                JsonNode messageObj = responseNode.path("choices").get(0).path("message");
                if (messageObj.has("reasoning_content") && !messageObj.path("reasoning_content").isNull()) {
                    reasoning = messageObj.path("reasoning_content").asText();
                }
            } else {
                throw new RuntimeException("API Call failed with status: " + response.statusCode());
            }

        } catch (Exception e) {
            // Fallback to basic heuristics if API fails
            forgery = (docs == null || docs.isEmpty()) ? 50.0 : 5.0;
            duplicate = (claims == null || claims.isEmpty()) ? 0.0 : 15.0;
            risk = (frauds == null || frauds.isEmpty()) ? 0.0 : 25.0;
            trust = Math.max(100.0 - (forgery * 0.4 + duplicate * 0.4 + risk * 0.2), 0.0);
            ownershipMatch = (docs != null && !docs.isEmpty());
            confidence = 40.0;
            summary = "Fallback verification used due to AI service timeout. " + e.getMessage();
            reasoning = "N/A";
        }

        AiVerification report = new AiVerification();
        report.setProperty(property);
        report.setAiTrustScore(BigDecimal.valueOf(trust));
        report.setForgeryScore(BigDecimal.valueOf(forgery));
        report.setDuplicateScore(BigDecimal.valueOf(duplicate));
        report.setOwnershipMatch(ownershipMatch);
        report.setRiskScore(BigDecimal.valueOf(risk));
        report.setConfidence(BigDecimal.valueOf(Math.min(trust + 5.0, 100.0)));
        report.setSummary(summary);
        report.setReasoning(reasoning);
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
