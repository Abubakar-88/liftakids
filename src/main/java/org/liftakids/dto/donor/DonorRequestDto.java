package org.liftakids.dto.donor;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.liftakids.entity.DonorType;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DonorRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Pattern(
            regexp = "^(\\+\\d{1,3}[- ]?)?\\d{7,15}$",
            message = "Invalid international phone number"
    )
    private String phone;

    @NotBlank(message = "Address is required")
    private String address;

    @NotNull(message = "Donor type is required")
    private DonorType type;

    private boolean status = true;
}
