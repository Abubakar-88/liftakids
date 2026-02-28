package org.liftakids.dto.institute;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.InstitutionType;

import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateInstitutionDto {
    @NotBlank
    private String institutionName;

    @NotNull
    private Long divisionId;
    @NotNull
    private Long districtId;
    @NotNull
    private Long thanaId;
    @NotNull
    private Long unionOrAreaId;

    @NotNull
    private InstitutionType type;

    @NotBlank
    @Email
    private String email;

    @NotNull
    @Size(min = 10, max = 15)
    private String phone;

    @NotBlank
    private String teacherName;
    @NotBlank
    private String teacherDesignation;

    @NotBlank
    private LocalDateTime aboutInstitution;

    @NotBlank
    private String villageOrHouse;

}
