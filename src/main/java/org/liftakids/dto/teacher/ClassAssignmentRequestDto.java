package org.liftakids.dto.teacher;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClassAssignmentRequestDto {

    @NotBlank(message = "Class name is required")
    private String className; // "Class 1", "Class 2", etc.

    private String section; // "A", "B", or null

    @NotBlank(message = "Subject is required")
    private String subject;

    private Boolean isClassTeacher = false;

    private String academicYear;
}

