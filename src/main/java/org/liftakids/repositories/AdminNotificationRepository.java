package org.liftakids.repositories;

import org.liftakids.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AdminNotificationRepository extends JpaRepository<Notification, Long>,
        JpaSpecificationExecutor<Notification> {

}
