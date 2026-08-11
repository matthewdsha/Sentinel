package com.sentinel.notification_service.service;

import com.sentinel.notification_service.event.InventoryRejectedEvent;
import com.sentinel.notification_service.event.InventoryReservedEvent;
import com.sentinel.notification_service.model.Notification;
import com.sentinel.notification_service.repository.NotificationRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class NotificationService {
    private final NotificationRepository notificationRepository;

    public NotificationService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> getNotifications() {
        return (List<Notification>) notificationRepository.findAll();
    }

    @KafkaListener(topics = "inventory-reserved", groupId = "notification-service")
    public void handleInventoryReserved(InventoryReservedEvent event) {
        Notification notification = new Notification();
        notification.setOrderId(event.orderId());
        notification.setMessage("Item " + event.sku() + " reserved for order " + event.orderId());
        notification.setTimestamp(Instant.now());
        notificationRepository.save(notification);
    }

    @KafkaListener(topics = "inventory-rejected", groupId = "notification-service")
    public void handleInventoryRejected(InventoryRejectedEvent event) {
        Notification notification = new Notification();
        notification.setOrderId(event.orderId());
        notification.setMessage("Item " + event.sku() + " rejected because of reason: " + event.reason());
        notification.setTimestamp(Instant.now());
        notificationRepository.save(notification);
    }
}
