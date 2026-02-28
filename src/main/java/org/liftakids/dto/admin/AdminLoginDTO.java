package org.liftakids.dto.admin;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AdminLoginDTO {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
