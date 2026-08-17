package com.sentinel.notification_service.service;

import com.sentinel.notification_service.event.InventoryRejectedEvent;
import com.sentinel.notification_service.event.InventoryReservedEvent;
import com.sentinel.notification_service.event.RejectionReason;
import com.sentinel.notification_service.model.Notification;
import com.sentinel.notification_service.repository.NotificationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private NotificationService notificationService;

    @Test
    void saveNotification_whenInventoryReserved() {
        InventoryReservedEvent event = new InventoryReservedEvent(1L, "WIDGET-1", 2, Instant.now());

        notificationService.handleInventoryReserved(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(1L);
        assertThat(saved.getMessage()).contains("WIDGET-1");
        assertThat(saved.getMessage()).contains("reserved");
    }

    @Test
    void saveNotification_whenInventoryRejected() {
        InventoryRejectedEvent event = new InventoryRejectedEvent(2L, "WIDGET-1", RejectionReason.INSUFFICIENT_STOCK, Instant.now());

        notificationService.handleInventoryRejected(event);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationRepository).save(captor.capture());

        Notification saved = captor.getValue();
        assertThat(saved.getOrderId()).isEqualTo(2L);
        assertThat(saved.getMessage()).contains("WIDGET-1");
        assertThat(saved.getMessage()).contains("rejected");
        assertThat(saved.getMessage()).contains("INSUFFICIENT_STOCK");
    }

    @Test
    void getNotifications_returnWhatRepositoryProvides() {
        Notification notification = new Notification();
        notification.setOrderId(3L);
        notification.setMessage("This is a test message.");
        notification.setTimestamp(Instant.now());

        when(notificationRepository.findAll()).thenReturn(List.of(notification));

        List<Notification> notifications = notificationService.getNotifications();

        assertThat(notifications).hasSize(1);
        assertThat(notifications.getFirst().getOrderId()).isEqualTo(3L);
    }
}
