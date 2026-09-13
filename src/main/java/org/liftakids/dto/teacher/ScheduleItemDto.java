package org.liftakids.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleItemDto {
    private String className;
    private String subject;
    private String day;
    private String startTime; // "09:00"
    private String endTime;   // "10:00"
    private String room;
}