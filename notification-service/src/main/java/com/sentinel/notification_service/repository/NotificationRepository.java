package com.sentinel.notification_service.repository;

import com.sentinel.notification_service.model.Notification;
import org.springframework.data.repository.CrudRepository;

public interface NotificationRepository extends CrudRepository<Notification, String> {
}
