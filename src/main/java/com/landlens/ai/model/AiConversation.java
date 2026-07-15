package com.landlens.ai.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.landlens.common.BaseAuditEntity;
import com.landlens.user.model.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "ai_conversations")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class AiConversation extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"passwordHash", "role", "createdAt", "updatedAt", "createdBy", "updatedBy", "isActive"})
    private User user;

    @Column(name = "title", length = 150)
    private String title;
}
