package org.liftakids.dto.donor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.liftakids.entity.DonorType;

@Data
@AllArgsConstructor
public class DonorUpdateRequestDto {
    @NotBlank(message = "Name is required")
    private String name;

    @Email(message = "Email should be valid")
    private String email;

    @NotNull(message = "Donor type is required")
    private DonorType type;

    private String phone;

    private String address;
}
