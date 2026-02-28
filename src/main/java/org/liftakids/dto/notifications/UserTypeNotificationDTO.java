package org.liftakids.dto.notifications;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserTypeNotificationDTO {
    @NotBlank(message = "User type is required")
    private String userType; // "DONOR", "INSTITUTION", "ADMIN"

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message is required")
    private String message;

    private String actionUrl;

    @NotNull(message = "Admin ID is required")
    private Long adminId;
}
