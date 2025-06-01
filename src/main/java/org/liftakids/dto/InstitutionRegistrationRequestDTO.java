package org.liftakids.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InstitutionRegistrationRequestDTO {
    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String phone;

    @NotBlank
    private String registrationNumber;

    private String addressDetails;

    @NotNull
    private Long divisionId;

    @NotNull
    private Long districtId;

    @NotNull
    private Long thanaId;

    @NotNull
    private Long unionId;

    @NotBlank
    @Size(min = 6)
    private String password;
}
