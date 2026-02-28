package org.liftakids.dto.admin;

import lombok.Data;

@Data
public class AdminResponseDTO {
    private Long adminId;
    private String name;
    private String email;
    private String username;
    private boolean active;
    private int approvedInstitutionsCount;
}
