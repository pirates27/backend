package com.landlens.notification.mapper;

import com.landlens.notification.dto.NotificationResponseDto;
import com.landlens.notification.model.Notification;

public class NotificationMapper {

    public static NotificationResponseDto toResponseDto(Notification notif) {
        if (notif == null) {
            return null;
        }
        NotificationResponseDto dto = new NotificationResponseDto();
        dto.setId(notif.getId());
        dto.setTitle(notif.getTitle());
        dto.setMessage(notif.getMessage());
        dto.setType(notif.getType());
        dto.setIsRead(notif.getIsRead());
        if (notif.getReceiver() != null) {
            dto.setReceiverId(notif.getReceiver().getId());
        }
        dto.setCreatedTime(notif.getCreatedTime());
        return dto;
    }
}
