package org.liftakids.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.enm.InstitutionStatus;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AdminActionDto {
    private Long institutionId;
    private InstitutionStatus action;
    private String rejectionReason;
}
