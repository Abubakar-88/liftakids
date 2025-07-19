package org.liftakids.dto.institute;

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
    private Long unionOrAreaId;
    private InstitutionType type;
    private String email;
    private String phone;
    private String villageOrHouse;
    private Boolean approved;
    private LocalDateTime registrationDate;
    private LocalDateTime updateDate;
}
