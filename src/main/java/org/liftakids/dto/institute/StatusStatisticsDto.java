package org.liftakids.dto.institute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatusStatisticsDto {
    private Long total;
    private Long approved;
    private Long pending;
    private Long rejected;
    private Long suspended;
}
