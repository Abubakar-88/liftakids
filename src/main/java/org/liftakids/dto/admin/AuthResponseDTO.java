package org.liftakids.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private boolean success;
    private String message;
    private String token;
    private AdminResponseDTO admin;
    private String userType = "ADMIN";
}
