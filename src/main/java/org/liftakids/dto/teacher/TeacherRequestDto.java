package org.liftakids.dto.teacher;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
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
    private String emergencyContact;

    // 👇 Qualifications
    private List<QualificationRequestDto> qualifications;

    // 👇 Experiences
    private List<ExperienceRequestDto> experiences;
}
