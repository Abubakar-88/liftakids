package org.liftakids.dto.notifications;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.enm.ExportFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExportRequestDTO {
    private NotificationFilterDTO filter;
    private ExportFormat format; // EXCEL, CSV, PDF
}
