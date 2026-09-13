package org.liftakids.dto.teacher;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherAttendanceDto {

    private Long teacherId;
    private String teacherName;

    // Statistics
    private Long totalPresent;
    private Long totalAbsent;
    private Long totalLeave;
    private Long totalDays;

    // Percentage
    private Double attendancePercentage; // (present / total) * 100
    private Double leavePercentage;      // (leave / total) * 100

    // Today's Status
    private String todayStatus; // PRESENT, ABSENT, LEAVE, LATE
    private LocalDate todayDate;
    private String checkInTime;
    private String checkOutTime;

    // Date Range
    private LocalDate fromDate;
    private LocalDate toDate;
}
