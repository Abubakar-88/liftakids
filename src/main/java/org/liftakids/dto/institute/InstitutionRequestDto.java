package org.liftakids.dto.institute;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.InstitutionType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionRequestDto {

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
    private String teacherName;

    @NotBlank
    private String teacherDesignation;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 10, max = 15)
    private String phone;

    private String villageOrHouse;

    @NotBlank
    private String password;

    @NotBlank
    private String aboutInstitution;

    private Boolean approved;
}
