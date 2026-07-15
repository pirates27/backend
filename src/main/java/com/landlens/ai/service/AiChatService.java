package com.landlens.ai.service;

import com.landlens.ai.model.AiConversation;
import com.landlens.ai.model.AiMessage;
import com.landlens.ai.repository.AiConversationRepository;
import com.landlens.ai.repository.AiMessageRepository;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class AiChatService {

    @Autowired
    private AiConversationRepository conversationRepository;

    @Autowired
    private AiMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public AiConversation startConversation(UUID userId, String title) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        AiConversation conversation = new AiConversation();
        conversation.setUser(user);
        conversation.setTitle(title != null ? title : "Chat with AI Assistant");
        conversation.setIsActive(true);

        return conversationRepository.save(conversation);
    }

    public List<AiConversation> getUserConversations(UUID userId) {
        return conversationRepository.findByUserIdAndIsActiveTrue(userId);
    }

    public List<AiMessage> getMessages(UUID conversationId) {
        return messageRepository.findByConversationIdAndIsActiveTrueOrderByTimestampAsc(conversationId);
    }

    @Transactional
    public AiMessage sendMessage(UUID conversationId, String content) {
        AiConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new RuntimeException("Conversation thread not found"));

        // Save User Message
        AiMessage userMsg = new AiMessage();
        userMsg.setConversation(conversation);
        userMsg.setSenderRole("USER");
        userMsg.setContent(content);
        userMsg.setTimestamp(Instant.now());
        userMsg.setIsActive(true);
        messageRepository.save(userMsg);

        // Generate Mock AI Response
        String query = content.toLowerCase();
        String aiResponseText;
        if (query.contains("patta") || query.contains("deed")) {
            aiResponseText = "Patta and Sale Deeds are official registry titles. LandLens AI scans OCR transcripts to check if the uploaded document owner matches the property listing provider.";
        } else if (query.contains("trust") || query.contains("score")) {
            aiResponseText = "The AI Trust Score is derived from bounds overlap, OCR verification, and history checks. Any score above 80 is considered high trust.";
        } else if (query.contains("timeline") || query.contains("status")) {
            aiResponseText = "The verification timeline logs every transition state. Properties progress from PENDING_AI -> PENDING_GOVT -> APPROVED or REJECTED.";
        } else {
            aiResponseText = "Hello! I am your LandLens AI verification assistant. Ask me questions about property trust scores, document OCR status, or timelines.";
        }

        AiMessage aiMsg = new AiMessage();
        aiMsg.setConversation(conversation);
        aiMsg.setSenderRole("AI");
        aiMsg.setContent(aiResponseText);
        aiMsg.setTimestamp(Instant.now().plusMillis(500)); // slightly offset
        aiMsg.setIsActive(true);
        
        return messageRepository.save(aiMsg);
    }
}
