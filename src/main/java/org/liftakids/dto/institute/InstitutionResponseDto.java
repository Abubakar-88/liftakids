package org.liftakids.dto.institute;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.InstitutionType;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InstitutionResponseDto {
    private Long institutionsId;
    private String institutionName;
    private Long divisionId;
    private Long districtId;
    private Long thanaId;
    private Long unionOrAreaId;
    private InstitutionType type;
    private String teacherName;
    private String teacherDesignation;
    private String email;
    private String phone;
    private String villageOrHouse;
    private Boolean approved;
    private LocalDateTime registrationDate;
    private LocalDateTime updateDate;
}
