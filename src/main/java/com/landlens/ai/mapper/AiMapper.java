package com.landlens.ai.mapper;

import com.landlens.ai.dto.AiConversationResponseDto;
import com.landlens.ai.dto.AiMessageResponseDto;
import com.landlens.ai.dto.AiVerificationResponseDto;
import com.landlens.ai.model.AiConversation;
import com.landlens.ai.model.AiMessage;
import com.landlens.ai.model.AiVerification;

public class AiMapper {

    public static AiConversationResponseDto toResponseDto(AiConversation convo) {
        if (convo == null) {
            return null;
        }
        AiConversationResponseDto dto = new AiConversationResponseDto();
        dto.setId(convo.getId());
        dto.setTitle(convo.getTitle());
        if (convo.getUser() != null) {
            dto.setUserId(convo.getUser().getId());
        }
        dto.setCreatedAt(convo.getCreatedAt());
        return dto;
    }

    public static AiMessageResponseDto toResponseDto(AiMessage msg) {
        if (msg == null) {
            return null;
        }
        AiMessageResponseDto dto = new AiMessageResponseDto();
        dto.setId(msg.getId());
        if (msg.getConversation() != null) {
            dto.setConversationId(msg.getConversation().getId());
        }
        dto.setSenderRole(msg.getSenderRole());
        dto.setContent(msg.getContent());
        dto.setTimestamp(msg.getTimestamp());
        return dto;
    }

    public static AiVerificationResponseDto toResponseDto(AiVerification verify) {
        if (verify == null) {
            return null;
        }
        AiVerificationResponseDto dto = new AiVerificationResponseDto();
        dto.setId(verify.getId());
        if (verify.getProperty() != null) {
            dto.setPropertyId(verify.getProperty().getId());
        }
        dto.setAiTrustScore(verify.getAiTrustScore());
        dto.setForgeryScore(verify.getForgeryScore());
        dto.setDuplicateScore(verify.getDuplicateScore());
        dto.setOwnershipMatch(verify.getOwnershipMatch());
        dto.setRiskScore(verify.getRiskScore());
        dto.setSummary(verify.getSummary());
        dto.setConfidence(verify.getConfidence());
        dto.setReasoning(verify.getReasoning());
        dto.setGeneratedDate(verify.getGeneratedDate());
        return dto;
    }
}
