package org.liftakids.dto.teacher;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponseDto {

    private Long id;
    private String teacherId;
    private String name;
    private String email;
    private String phone;
    private String address;
    private String photoUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private LocalDate joiningDate;
    private String designation;
    private String department;
    private String specialization;
    private boolean active;
    private boolean onLeave;
    private String leaveReason;
    private String emergencyContact;
    private Long institutionId;
    private String institutionName;

    private List<QualificationResponseDto> qualifications;
    private List<ExperienceResponseDto> experiences;
    private List<ClassAssignmentResponseDto> classAssignments;
    private List<ScheduleResponseDto> schedules;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Additional stats
    private Integer totalStudents;
    private Integer totalClasses;
    private Double attendancePercentage;
}


