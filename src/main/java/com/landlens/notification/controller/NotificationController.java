package com.landlens.notification.controller;

import com.landlens.notification.dto.NotificationResponseDto;
import com.landlens.notification.mapper.NotificationMapper;
import com.landlens.notification.model.Notification;
import com.landlens.notification.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponseDto>> getMyNotifications(Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        List<Notification> list = notificationService.getUserNotifications(userId);
        List<NotificationResponseDto> dtoList = list.stream()
                .map(NotificationMapper::toResponseDto)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtoList);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponseDto> markAsRead(@PathVariable UUID id, Principal principal) {
        UUID userId = UUID.fromString(principal.getName());
        Notification readNotification = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(NotificationMapper.toResponseDto(readNotification));
    }
}
