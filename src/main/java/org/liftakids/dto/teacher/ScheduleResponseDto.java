package org.liftakids.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleResponseDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private String className;
    private String subject;
    private String day;
    private String startTime;
    private String endTime;
    private String room;
    private Boolean isActive;
}
