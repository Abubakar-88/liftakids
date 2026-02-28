package org.liftakids.dto.notifications;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BulkOperationDTO {
    @NotNull(message = "Notification IDs are required")
    private List<Long> notificationIds;

    @NotNull(message = "Admin ID is required")
    private Long adminId;
}
