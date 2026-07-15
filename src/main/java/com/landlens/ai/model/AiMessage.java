package com.landlens.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.landlens.common.BaseAuditEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "ai_messages")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AiMessage extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    @JsonIgnoreProperties({"user", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private AiConversation conversation;

    @Column(name = "sender_role", length = 20, nullable = false)
    private String senderRole; // USER, AI

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp = Instant.now();
}
