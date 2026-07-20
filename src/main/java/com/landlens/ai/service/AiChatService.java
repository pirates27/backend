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
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;

@Service
public class AiChatService {

    private static final String CONTENT_KEY = "content";

    @Autowired
    private AiConversationRepository conversationRepository;

    @Autowired
    private AiMessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${openai.api.key}")
    private String openAiApiKey;

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

        String aiResponseText = "";
        try {
            // Get conversation history
            List<AiMessage> history = messageRepository.findByConversationIdAndIsActiveTrueOrderByTimestampAsc(conversationId);
            
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", "openai/gpt-oss-120b");
            requestBody.put("temperature", 0.7);
            requestBody.put("max_tokens", 1024);
            
            ArrayNode messagesArray = requestBody.putArray("messages");
            
            // System prompt
            ObjectNode systemMsg = messagesArray.addObject();
            systemMsg.put("role", "system");
            systemMsg.put(CONTENT_KEY, "You are LandLens AI, an expert property verification assistant in India. You help users understand property trust scores, land documents like Patta and Sale Deeds, and verification timelines. Keep your answers concise, helpful, and professional.\n\nIMPORTANT: Use Markdown tables when presenting structured data. If you recommend a specific property and you know its propertyId, you MUST output a JSON block like this so the frontend can render an interactive Property Card:\n```json\n{ \"type\": \"property\", \"propertyId\": \"the-uuid-here\" }\n```");
            
            // Add history
            for (AiMessage msg : history) {
                ObjectNode msgNode = messagesArray.addObject();
                String role = msg.getSenderRole().equalsIgnoreCase("USER") ? "user" : "assistant";
                msgNode.put("role", role);
                msgNode.set(CONTENT_KEY, msgNode.textNode(msg.getContent()));
            }

            try (HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(15))
                    .build()) {

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create("https://integrate.api.nvidia.com/v1/chat/completions"))
                        .header("Authorization", "Bearer " + openAiApiKey)
                        .header("Content-Type", "application/json")
                        .header("Accept", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                        .build();
    
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
    
                if (response.statusCode() == 200) {
                    JsonNode root = objectMapper.readTree(response.body());
                    aiResponseText = root.path("choices").path(0).path("message").path(CONTENT_KEY).asText();
                } else {
                    aiResponseText = "Sorry, I am currently facing technical issues reaching the AI server. Code: " + response.statusCode();
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            aiResponseText = "Sorry, your request was interrupted: " + e.getMessage();
        } catch (Exception e) {
            aiResponseText = "Sorry, an internal error occurred while processing your request: " + e.getMessage();
        }

        AiMessage aiMsg = new AiMessage();
        aiMsg.setConversation(conversation);
        aiMsg.setSenderRole("AI");
        aiMsg.setContent(aiResponseText);
        aiMsg.setTimestamp(Instant.now().plusMillis(500));
        aiMsg.setIsActive(true);
        
        return messageRepository.save(aiMsg);
    }
}
