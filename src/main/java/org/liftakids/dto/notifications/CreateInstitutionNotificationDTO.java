package org.liftakids.dto.notifications;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.enm.NotificationType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstitutionNotificationDTO {
    @NotNull(message = "Institution ID is required")
    private Long institutionId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    @NotNull(message = "Notification type is required")
    private NotificationType type;

    private String actionUrl;
    private String relatedEntityType;
    private Long relatedEntityId;

    @NotNull(message = "Admin ID is required")
    private Long adminId;
}
