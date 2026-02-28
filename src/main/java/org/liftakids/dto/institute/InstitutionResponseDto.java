package org.liftakids.dto.institute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.InstitutionType;
import org.liftakids.entity.SystemAdmin;
import org.liftakids.entity.enm.InstitutionStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class InstitutionResponseDto {
    private Long institutionsId;
    private String institutionName;
    private Long divisionId;
    private Long districtId;
    private Long thanaId;
    private Long unionOrAreaId;
    private String villageOrHouse;
    private InstitutionType type;
    private String email;
    private String phone;
    private String teacherName;

    private String teacherDesignation;
    private String aboutInstitution;
    private Boolean approved;


    @JsonIgnore
    private SystemAdmin approvedBy;

    private LocalDateTime approvalDate;
    private String approvalNotes;
    private LocalDateTime registrationDate;
    private InstitutionStatus status;

    @JsonIgnore
    private SystemAdmin rejectedBy;

    private LocalDateTime rejectionDate;
    private String rejectionReason;

    @JsonIgnore
    private SystemAdmin suspendedBy;

    private LocalDateTime suspendedDate;
    private String suspendedReason;
    private LocalDateTime updateDate;
}
