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
public class TeacherUpdateDto {
    private String name;
    private String email;
    private String phone;
    private String address;
    private String photoUrl;
    private String gender;
    private LocalDate dateOfBirth;
    private String designation;
    private String department;
    private String specialization;
    private String emergencyContact;
    private Boolean active;
    private Boolean onLeave;
    private String leaveReason;
}