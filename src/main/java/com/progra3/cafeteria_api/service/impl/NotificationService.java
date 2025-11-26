package com.progra3.cafeteria_api.service.impl;

import com.progra3.cafeteria_api.model.dto.NotificationResponseDTO;
import com.progra3.cafeteria_api.model.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final SimpMessagingTemplate messagingTemplate;

    public void sendNotification(NotificationType type, String message, Object data, Long businessId) {
        NotificationResponseDTO notification = new NotificationResponseDTO(
                type,
                message,
                LocalDateTime.now(),
                data,
                businessId
        );

        String destination = "/topic/notifications/" + businessId;
        messagingTemplate.convertAndSend(destination, notification);

        log.info("Notification sent: {} to business {}", type, businessId);
    }

    public void sendNotification(NotificationType type, String message, Long businessId) {
        sendNotification(type, message, null, businessId);
    }
}

