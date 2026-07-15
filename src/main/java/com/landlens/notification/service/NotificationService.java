package com.landlens.notification.service;

import com.landlens.notification.model.Notification;
import com.landlens.notification.repository.NotificationRepository;
import com.landlens.user.model.User;
import com.landlens.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Notification sendNotification(UUID receiverId, String title, String message, String type) {
        User receiver = userRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Receiver user not found"));

        Notification notification = new Notification();
        notification.setReceiver(receiver);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type.toUpperCase());
        notification.setIsRead(false);
        notification.setCreatedTime(Instant.now());
        notification.setIsActive(true);

        return notificationRepository.save(notification);
    }

    public List<Notification> getUserNotifications(UUID userId) {
        return notificationRepository.findByReceiverIdAndIsActiveTrueOrderByCreatedTimeDesc(userId);
    }

    @Transactional
    public Notification markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        if (!notification.getReceiver().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized to modify this notification");
        }

        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }
}
