package com.landlens.notification.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
public class NotificationResponseDto {
    private UUID id;
    private String title;
    private String message;
    private String type;
    private Boolean isRead;
    private UUID receiverId;
    private Instant createdTime;
}
